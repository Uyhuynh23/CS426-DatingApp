import { onCall, onRequest, HttpsError } from "firebase-functions/v2/https";
import * as admin from "firebase-admin";
import { setGlobalOptions } from "firebase-functions/v2";

// Set global options
setGlobalOptions({ region: "asia-southeast1" });

admin.initializeApp();
const db = admin.firestore();

// Rate limiting storage (in production, use Redis or Firestore)
const rateLimitCache: { [key: string]: Date[] } = {};

// Configuration
const MAX_QUERIES_PER_HOUR = 30;
const RECOMMENDATION_BATCH_SIZE = 10;
const MAX_DISTANCE_KM = 50;
const FIRESTORE_IN_LIMIT = 30;

// Helper functions
function chunkArray<T>(array: T[], chunkSize: number): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < array.length; i += chunkSize) {
    chunks.push(array.slice(i, i + chunkSize));
  }
  return chunks;
}

function checkRateLimit(userId: string): boolean {
  const currentTime = new Date();
  const userKey = `rate_limit_${userId}`;

  if (!rateLimitCache[userKey]) {
    rateLimitCache[userKey] = [];
  }

  // Remove old timestamps (older than 1 hour)
  rateLimitCache[userKey] = rateLimitCache[userKey].filter(
    timestamp => (currentTime.getTime() - timestamp.getTime()) < 3600000
  );

  // Check if limit exceeded
  if (rateLimitCache[userKey].length >= MAX_QUERIES_PER_HOUR) {
    return false;
  }

  // Add current timestamp
  rateLimitCache[userKey].push(currentTime);
  return true;
}

function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // Earth's radius in kilometers

  const dlat = (lat2 - lat1) * Math.PI / 180;
  const dlon = (lon2 - lon1) * Math.PI / 180;

  const a = Math.sin(dlat/2) * Math.sin(dlat/2) +
           Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
           Math.sin(dlon/2) * Math.sin(dlon/2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  const distance = R * c;

  return distance;
}

function calculateCompatibilityScore(user1: any, user2: any): number {
  let score = 0;
  const maxScore = 100;

  // Age compatibility (30 points)
  const age1 = user1.age || 25;
  const age2 = user2.age || 25;
  const ageDiff = Math.abs(age1 - age2);
  const ageScore = Math.max(0, 30 - (ageDiff * 2));
  score += ageScore;

  // Interest compatibility (40 points)
  const interests1 = new Set(user1.interests || []);
  const interests2 = new Set(user2.interests || []);

  if (interests1.size > 0 && interests2.size > 0) {
    const commonInterests = [...interests1].filter(x => interests2.has(x)).length;
    const totalInterests = new Set([...interests1, ...interests2]).size;
    const interestScore = totalInterests > 0 ? (commonInterests / totalInterests) * 40 : 0;
    score += interestScore;
  }

  // Location proximity (20 points)
  if (user1.location && user2.location) {
    const distance = calculateDistance(
      user1.location.lat || 0,
      user1.location.lng || 0,
      user2.location.lat || 0,
      user2.location.lng || 0
    );
    const locationScore = Math.max(0, 20 - (distance * 0.4));
    score += locationScore;
  }

  // Education level (10 points)
  if (user1.education === user2.education) {
    score += 10;
  }

  return Math.min(score, maxScore);
}

/**
 * Verify Google ID token and create/update user
 */
export const verifyGoogleToken = onCall(async (req) => {
  try {
    const { idToken } = req.data;
    if (!idToken) {
      throw new HttpsError("invalid-argument", "ID token is required");
    }

    // Verify the ID token
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    const uid = decodedToken.uid;
    const email = decodedToken.email;
    const name = decodedToken.name;
    const picture = decodedToken.picture;

    // Check if user exists
    const userRef = db.collection("users").doc(uid);
    const userDoc = await userRef.get();

    const userData = {
      uid,
      email,
      name,
      picture,
      lastLogin: admin.firestore.FieldValue.serverTimestamp(),
      provider: "google"
    };

    if (!userDoc.exists) {
      // New user - create profile
      const newUserData = {
        ...userData,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        age: null,
        bio: "",
        interests: [],
        location: null,
        photos: picture ? [picture] : [],
        isProfileComplete: false
      };
      await userRef.set(newUserData);
      return { success: true, user: newUserData, isNewUser: true };
    } else {
      // Existing user - update last login
      await userRef.update({
        lastLogin: admin.firestore.FieldValue.serverTimestamp(),
        picture
      });
      const existingData = userDoc.data();
      return { success: true, user: { ...existingData, ...userData }, isNewUser: false };
    }
  } catch (error) {
    console.error("Error verifying Google token:", error);
    throw new HttpsError("internal", `Authentication failed: ${error}`);
  }
});

/**
 * Get multiple users by IDs, handling Firestore whereIn limit of 30
 */
export const getUsersByIds = onCall(async (req) => {
  const ids: string[] = req.data.ids;
  if (!ids || !Array.isArray(ids)) {
    throw new HttpsError("invalid-argument", "ids array is required");
  }

  if (ids.length === 0) {
    return { success: true, users: [] };
  }

  // Split IDs into chunks of 30 (Firestore whereIn limit)
  const idChunks = chunkArray(ids, FIRESTORE_IN_LIMIT);
  const allUsers: any[] = [];

  // Process each chunk
  for (const chunk of idChunks) {
    if (chunk.length === 0) continue;

    const snapshot = await db.collection("users")
      .where(admin.firestore.FieldPath.documentId(), "in", chunk)
      .get();

    snapshot.forEach(doc => {
      const userData = doc.data();
      userData.user_id = doc.id;
      allUsers.push(userData);
    });
  }

  console.log(`Retrieved ${allUsers.length} users from ${ids.length} requested IDs`);

  return {
    success: true,
    users: allUsers,
    total_requested: ids.length,
    total_found: allUsers.length
  };
});

/**
 * Get user recommendations with advanced matching algorithm
 */
export const getRecommendations = onCall(async (req) => {
  try {
    const userId = req.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "User must be authenticated");
    }

    // Check rate limit
    if (!checkRateLimit(userId)) {
      throw new HttpsError("resource-exhausted", "Rate limit exceeded. Maximum 30 queries per hour.");
    }

    // Get current user data
    const userDoc = await db.collection("users").doc(userId).get();
    if (!userDoc.exists) {
      throw new HttpsError("not-found", "User not found");
    }

    const currentUser = userDoc.data();

    // Get users that current user has already seen/swiped
    const seenUsers = new Set<string>();
    const swipesSnapshot = await db.collection("swipes")
      .where("user_id", "==", userId)
      .get();

    swipesSnapshot.forEach(doc => {
      const swipeData = doc.data();
      seenUsers.add(swipeData.target_user_id);
    });

    // Get all potential matches (excluding seen users and current user)
    const usersSnapshot = await db.collection("users")
      .where("isProfileComplete", "==", true)
      .limit(100)
      .get();

    const potentialMatches: any[] = [];

    usersSnapshot.forEach(doc => {
      const userData = doc.data();
      if (doc.id !== userId && !seenUsers.has(doc.id) && userData.isProfileComplete) {
        // Calculate compatibility score
        const score = calculateCompatibilityScore(currentUser, userData);
        userData.compatibility_score = score;
        userData.user_id = doc.id;
        potentialMatches.push(userData);
      }
    });

    // Sort by compatibility score and take top matches
    potentialMatches.sort((a, b) => b.compatibility_score - a.compatibility_score);
    let recommendations = potentialMatches.slice(0, RECOMMENDATION_BATCH_SIZE);

    // Add some randomization to avoid always showing the same users
    if (recommendations.length > 5) {
      const topHalf = recommendations.slice(0, Math.floor(recommendations.length / 2));
      const bottomHalf = recommendations.slice(Math.floor(recommendations.length / 2));

      // Shuffle bottom half
      for (let i = bottomHalf.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [bottomHalf[i], bottomHalf[j]] = [bottomHalf[j], bottomHalf[i]];
      }

      recommendations = [...topHalf, ...bottomHalf];
    }

    return {
      success: true,
      recommendations,
      count: recommendations.length
    };

  } catch (error) {
    console.error("Error getting recommendations:", error);
    throw new HttpsError("internal", `Failed to get recommendations: ${error}`);
  }
});

/**
 * Handle user swipe and check for matches
 */
export const handleSwipe = onCall(async (req) => {
  try {
    const userId = req.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "User must be authenticated");
    }

    const { target_user_id, is_like } = req.data;
    if (!target_user_id) {
      throw new HttpsError("invalid-argument", "target_user_id is required");
    }

    // Record the swipe
    const swipeData = {
      user_id: userId,
      target_user_id,
      is_like: is_like || false,
      created_at: admin.firestore.FieldValue.serverTimestamp()
    };

    await db.collection("swipes").add(swipeData);

    let isMatch = false;
    if (is_like) {
      // Check if target user also liked this user
      const targetSwipeSnapshot = await db.collection("swipes")
        .where("user_id", "==", target_user_id)
        .where("target_user_id", "==", userId)
        .where("is_like", "==", true)
        .get();

      if (!targetSwipeSnapshot.empty) {
        // It's a match!
        isMatch = true;
        const matchData = {
          user1_id: userId,
          user2_id: target_user_id,
          created_at: admin.firestore.FieldValue.serverTimestamp(),
          is_active: true
        };
        await db.collection("matches").add(matchData);

        // Create initial conversation
        const conversationData = {
          participants: [userId, target_user_id],
          created_at: admin.firestore.FieldValue.serverTimestamp(),
          last_message: null,
          last_message_time: null
        };
        await db.collection("conversations").add(conversationData);
      }
    }

    return {
      success: true,
      is_match: isMatch,
      swipe_recorded: true
    };

  } catch (error) {
    console.error("Error handling swipe:", error);
    throw new HttpsError("internal", `Failed to handle swipe: ${error}`);
  }
});

/**
 * Get user's matches
 */
export const getMatches = onCall(async (req) => {
  try {
    const userId = req.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "User must be authenticated");
    }

    // Get matches where user is either user1 or user2
    const matches1Promise = db.collection("matches")
      .where("user1_id", "==", userId)
      .where("is_active", "==", true)
      .get();

    const matches2Promise = db.collection("matches")
      .where("user2_id", "==", userId)
      .where("is_active", "==", true)
      .get();

    const [matches1Snapshot, matches2Snapshot] = await Promise.all([matches1Promise, matches2Promise]);

    const matches: any[] = [];

    // Process matches where user is user1
    for (const matchDoc of matches1Snapshot.docs) {
      const matchData = matchDoc.data();
      const otherUserId = matchData.user2_id;

      const otherUserDoc = await db.collection("users").doc(otherUserId).get();
      if (otherUserDoc.exists) {
        matches.push({
          match_id: matchDoc.id,
          other_user: { user_id: otherUserId, ...otherUserDoc.data() },
          created_at: matchData.created_at
        });
      }
    }

    // Process matches where user is user2
    for (const matchDoc of matches2Snapshot.docs) {
      const matchData = matchDoc.data();
      const otherUserId = matchData.user1_id;

      const otherUserDoc = await db.collection("users").doc(otherUserId).get();
      if (otherUserDoc.exists) {
        matches.push({
          match_id: matchDoc.id,
          other_user: { user_id: otherUserId, ...otherUserDoc.data() },
          created_at: matchData.created_at
        });
      }
    }

    // Sort by creation time (newest first)
    matches.sort((a, b) => b.created_at?.toMillis() - a.created_at?.toMillis());

    return {
      success: true,
      matches,
      count: matches.length
    };

  } catch (error) {
    console.error("Error getting matches:", error);
    throw new HttpsError("internal", `Failed to get matches: ${error}`);
  }
});

/**
 * Get user statistics and rate limit info
 */
export const getUserStats = onCall(async (req) => {
  try {
    const userId = req.auth?.uid;
    if (!userId) {
      throw new HttpsError("unauthenticated", "User must be authenticated");
    }

    // Get rate limit info
    const userKey = `rate_limit_${userId}`;
    const currentTime = new Date();

    let queriesThisHour = 0;
    if (rateLimitCache[userKey]) {
      rateLimitCache[userKey] = rateLimitCache[userKey].filter(
        timestamp => (currentTime.getTime() - timestamp.getTime()) < 3600000
      );
      queriesThisHour = rateLimitCache[userKey].length;
    }

    // Get user statistics
    const swipesSnapshot = await db.collection("swipes").where("user_id", "==", userId).get();
    const matches1Snapshot = await db.collection("matches").where("user1_id", "==", userId).get();
    const matches2Snapshot = await db.collection("matches").where("user2_id", "==", userId).get();

    const swipesCount = swipesSnapshot.size;
    const matchesCount = matches1Snapshot.size + matches2Snapshot.size;

    return {
      success: true,
      stats: {
        queries_this_hour: queriesThisHour,
        queries_remaining: MAX_QUERIES_PER_HOUR - queriesThisHour,
        total_swipes: swipesCount,
        total_matches: matchesCount
      }
    };

  } catch (error) {
    console.error("Error getting user stats:", error);
    throw new HttpsError("internal", `Failed to get user stats: ${error}`);
  }
});
