package com.aicloud.repository;

import com.aicloud.model.Plan;
import com.aicloud.model.User;
import com.aicloud.model.UserUsage;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Usuarios y consumos guardados en memoria. */
public class UserRepository {

    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserUsage> usages = new ConcurrentHashMap<>();

    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id.toLowerCase()));
    }

    /** Si el usuario no existe lo crea con plan FREE. */
    public User findOrCreate(String id) {
        return users.computeIfAbsent(id.toLowerCase(), key -> new User(key, Plan.FREE));
    }

    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public UserUsage usageOf(String userId) {
        return usages.computeIfAbsent(userId.toLowerCase(), key -> new UserUsage());
    }

    public Collection<UserUsage> allUsages() {
        return usages.values();
    }
}
