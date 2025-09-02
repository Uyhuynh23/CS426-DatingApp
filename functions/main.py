        return add_cors_headers({"error": str(e)}, 500)
        if request.method != "POST":
            return add_cors_headers({"error": "Method not allowed"}, 405)

        # Get user from Authorization header
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return add_cors_headers({"error": "Authorization required"}, 401)

        token = auth_header.split(" ")[1]
        decoded_token = auth.verify_id_token(token)
        user_id = decoded_token["uid"]

        data = request.get_json()
        if not data:
            return add_cors_headers({"error": "Request body required"}, 400)

        target_user_id = data.get("target_user_id")
        is_like = data.get("is_like", False)

        if not target_user_id:
            return add_cors_headers({"error": "target_user_id is required"}, 400)

        # Record the swipe
        swipe_data = {
            "user_id": user_id,
            "target_user_id": target_user_id,
            "is_like": is_like,
            "created_at": datetime.now()
        }

        db.collection("swipes").add(swipe_data)

        is_match = False
        if is_like:
            # Check if target user also liked this user
            target_swipe_query = db.collection("swipes").where(
                "user_id", "==", target_user_id
            ).where(
                "target_user_id", "==", user_id
            ).where(
                "is_like", "==", True
            )

            target_swipes = list(target_swipe_query.stream())

            if target_swipes:
                # It's a match!
                is_match = True
                match_data = {
                    "user1_id": user_id,
                    "user2_id": target_user_id,
                    "created_at": datetime.now(),
                    "is_active": True
                }
                db.collection("matches").add(match_data)

                # Create initial conversation
                conversation_data = {
                    "participants": [user_id, target_user_id],
                    "created_at": datetime.now(),
                    "last_message": None,
                    "last_message_time": None
                }
                db.collection("conversations").add(conversation_data)

        return add_cors_headers({
            "success": True,
            "is_match": is_match,
            "swipe_recorded": True
        })

    except Exception as e:
        logging.error(f"Error handling swipe: {str(e)}")
        else:
            queries_this_hour = 0

        # Get user statistics
        swipes_count = len(list(db.collection("swipes").where("user_id", "==", user_id).stream()))
        matches_count = len(list(db.collection("matches").where("user1_id", "==", user_id).stream())) + \
                      len(list(db.collection("matches").where("user2_id", "==", user_id).stream()))

        return https_fn.Response(
            json.dumps({
                "success": True,
                "stats": {
                    "queries_this_hour": queries_this_hour,
                    "queries_remaining": MAX_QUERIES_PER_HOUR - queries_this_hour,
                    "total_swipes": swipes_count,
                    "total_matches": matches_count
                }
            }),
            status=200,
            headers={"Content-Type": "application/json"}
        )

    except Exception as e:
        logging.error(f"Error getting user stats: {str(e)}")
        return https_fn.Response(
            json.dumps({"error": str(e)}),
            status=500,
            headers={"Content-Type": "application/json"}
        )

@https_fn.on_request(
    cors=options.CorsOptions(
        cors_origins=["*"],
        cors_methods=["GET", "POST", "OPTIONS"],
    )
)
def get_users_by_ids(req: https_fn.Request) -> https_fn.Response:
    """Get multiple users by IDs, handling Firestore whereIn limit of 30"""
    try:
        if req.method == "OPTIONS":
            return https_fn.Response("", status=204, headers={
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "POST, OPTIONS",
                "Access-Control-Allow-Headers": "Content-Type, Authorization",
            })

        if req.method != "POST":
            return https_fn.Response("Method not allowed", status=405)

        data = req.get_json()
        if not data or "ids" not in data:
            return https_fn.Response("ids array is required", status=400)

        ids = data["ids"]
        if not isinstance(ids, list):
            return https_fn.Response("ids must be an array", status=400)

        if len(ids) == 0:
            return https_fn.Response(
                json.dumps({"success": True, "users": []}),
                status=200,
                headers={"Content-Type": "application/json"}
            )

        # Split IDs into chunks of 30 (Firestore whereIn limit)
        id_chunks = chunk_list(ids, FIRESTORE_IN_LIMIT)
        all_users = []

        # Process each chunk
        for chunk in id_chunks:
            if len(chunk) == 0:
                continue

            # Query users with current chunk of IDs
            users_ref = db.collection("users")
            query = users_ref.where(firestore.FieldPath.document_id(), "in", chunk)
            docs = query.stream()

            # Add users from this chunk
            for doc in docs:
                user_data = doc.to_dict()
                user_data["user_id"] = doc.id
                all_users.append(user_data)

        logging.info(f"Retrieved {len(all_users)} users from {len(ids)} requested IDs")

        return https_fn.Response(
            json.dumps({
                "success": True,
                "users": all_users,
                "total_requested": len(ids),
                "total_found": len(all_users)
            }, default=str),
            status=200,
            headers={"Content-Type": "application/json"}
        )

    except Exception as e:
        logging.error(f"Error getting users by IDs: {str(e)}")
        return https_fn.Response(
            json.dumps({"error": str(e)}),
            status=500,
            headers={"Content-Type": "application/json"}
        )

@https_fn.on_request(
    cors=options.CorsOptions(
        cors_origins=["*"],
        cors_methods=["GET", "POST", "OPTIONS"],
    )
)
def get_users_by_filters(req: https_fn.Request) -> https_fn.Response:
    """Get users with advanced filtering, handling large result sets"""
    try:
        if req.method == "OPTIONS":
            return https_fn.Response("", status=204)

        if req.method != "POST":
            return https_fn.Response("Method not allowed", status=405)

        # Get user from Authorization header
        auth_header = req.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return https_fn.Response("Authorization required", status=401)

        token = auth_header.split(" ")[1]
        decoded_token = auth.verify_id_token(token)
        user_id = decoded_token["uid"]

        data = req.get_json()
        if not data:
            data = {}

        # Get filter parameters
        age_min = data.get("age_min")
        age_max = data.get("age_max")
        interests = data.get("interests", [])
        max_distance = data.get("max_distance", MAX_DISTANCE_KM)
        education = data.get("education")
        limit = min(data.get("limit", 50), 100)  # Max 100 users
        exclude_ids = data.get("exclude_ids", [])

        # Get current user's location for distance calculation
        current_user_doc = db.collection("users").document(user_id).get()
        current_user = current_user_doc.to_dict() if current_user_doc.exists else {}
        current_location = current_user.get("location")

        # Start with base query
        users_ref = db.collection("users")
        query = users_ref.where("isProfileComplete", "==", True)

        # Apply age filters if provided
        if age_min is not None:
            query = query.where("age", ">=", age_min)
        if age_max is not None:
            query = query.where("age", "<=", age_max)

        # Apply education filter if provided
        if education:
            query = query.where("education", "==", education)

        # Execute query and filter results
        docs = query.limit(limit * 2).stream()  # Get more to allow for filtering

        filtered_users = []
        for doc in docs:
            # Skip current user and excluded users
            if doc.id == user_id or doc.id in exclude_ids:
                continue

            user_data = doc.to_dict()
            user_data["user_id"] = doc.id

            # Filter by interests if provided
            if interests:
                user_interests = set(user_data.get("interests", []))
                if not any(interest in user_interests for interest in interests):
                    continue

            # Filter by distance if current user has location
            if current_location and "location" in user_data:
                distance = calculate_distance(
                    current_location.get("lat", 0),
                    current_location.get("lng", 0),
                    user_data["location"].get("lat", 0),
                    user_data["location"].get("lng", 0)
                )
                if distance > max_distance:
                    continue
                user_data["distance"] = round(distance, 1)

            filtered_users.append(user_data)

            # Stop if we have enough users
            if len(filtered_users) >= limit:
                break

        return https_fn.Response(
            json.dumps({
                "success": True,
                "users": filtered_users,
                "count": len(filtered_users)
            }, default=str),
            status=200,
            headers={"Content-Type": "application/json"}
        )

    except Exception as e:
        logging.error(f"Error filtering users: {str(e)}")
        return https_fn.Response(
            json.dumps({"error": str(e)}),
            status=500,
            headers={"Content-Type": "application/json"}
        )

@https_fn.on_request(
    cors=options.CorsOptions(
        cors_origins=["*"],
        cors_methods=["GET", "POST", "OPTIONS"],
    )
)
def batch_get_user_data(req: https_fn.Request) -> https_fn.Response:
    """Get user data for multiple users in batches, optimized for large lists"""
    try:
        if req.method == "OPTIONS":
            return https_fn.Response("", status=204)

        if req.method != "POST":
            return https_fn.Response("Method not allowed", status=405)

        # Get user from Authorization header
        auth_header = req.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return https_fn.Response("Authorization required", status=401)

        token = auth_header.split(" ")[1]
        decoded_token = auth.verify_id_token(token)
        requesting_user_id = decoded_token["uid"]

        data = req.get_json()
        if not data or "user_ids" not in data:
            return https_fn.Response("user_ids array is required", status=400)

        user_ids = data["user_ids"]
        if not isinstance(user_ids, list):
            return https_fn.Response("user_ids must be an array", status=400)

        # Remove duplicates and limit
        user_ids = list(set(user_ids))[:200]  # Max 200 users

        if len(user_ids) == 0:
            return https_fn.Response(
                json.dumps({"success": True, "users": {}}),
                status=200,
                headers={"Content-Type": "application/json"}
            )

        # Split into chunks of 30 for Firestore whereIn limit
        id_chunks = chunk_list(user_ids, FIRESTORE_IN_LIMIT)
        users_dict = {}

        # Process each chunk
        for chunk in id_chunks:
            if len(chunk) == 0:
                continue

            try:
                # Query users with current chunk of IDs
                users_ref = db.collection("users")
                query = users_ref.where(firestore.FieldPath.document_id(), "in", chunk)
                docs = query.stream()

                # Add users from this chunk to dictionary
                for doc in docs:
                    user_data = doc.to_dict()
                    # Only include essential fields for performance
                    essential_data = {
                        "user_id": doc.id,
                        "name": user_data.get("name", ""),
                        "age": user_data.get("age"),
                        "photos": user_data.get("photos", []),
                        "bio": user_data.get("bio", ""),
                        "interests": user_data.get("interests", []),
                        "location": user_data.get("location"),
                        "lastLogin": user_data.get("lastLogin")
                    }
                    users_dict[doc.id] = essential_data

            except Exception as chunk_error:
                logging.error(f"Error processing chunk {chunk}: {str(chunk_error)}")
                continue

        # Log statistics
        found_count = len(users_dict)
        requested_count = len(user_ids)
        logging.info(f"Batch user query: {found_count}/{requested_count} users found")

        return https_fn.Response(
            json.dumps({
                "success": True,
                "users": users_dict,
                "total_requested": requested_count,
                "total_found": found_count,
                "missing_ids": [uid for uid in user_ids if uid not in users_dict]
            }, default=str),
            status=200,
            headers={"Content-Type": "application/json"}
        )

    except Exception as e:
        logging.error(f"Error in batch_get_user_data: {str(e)}")
        return https_fn.Response(
            json.dumps({"error": str(e)}),
            status=500,
            headers={"Content-Type": "application/json"}
        )
