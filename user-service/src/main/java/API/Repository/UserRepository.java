/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import API.Entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jacobwowkjorgensen
 */
@Repository
public class UserRepository implements IUserRepository {

    private Connection connection;

    public UserRepository(
            @Value("${database.connection}") String connection,
            @Value("${database.username}") String username,
            @Value("${database.password}") String password) {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(connection, username, password);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList();
        try (PreparedStatement getUsers = this.connection.prepareStatement("SELECT *  FROM users WHERE active=true")) {
            ResultSet usersResult = getUsers.executeQuery();

            while (usersResult.next()) {
                users.add(new User((UUID) usersResult.getObject("id"), usersResult.getString("username"), usersResult.getString("email"), usersResult.getString("adress"), usersResult.getString("role"), usersResult.getString("cpr")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public boolean usernameExist(String username) {
        User user = findUserByUsername(username);
        return user != null;
    }

    @Override
    public User CreateUser(User user) {
        if (usernameExist(user.getUsername())) {
            return null;
        }

        try {
            connection.setAutoCommit(false);
            PreparedStatement createUser = this.connection.prepareStatement("INSERT INTO users VALUES (?, ?, ? , ? , ? , ? , ?, ? ) RETURNING id, username, email, active, adress, role, cpr");
            createUser.setObject(1, UUID.randomUUID(), Types.OTHER);
            createUser.setString(2, user.getUsername());
            createUser.setString(3, user.getPassword());
            createUser.setString(4, user.getEmail());
            createUser.setBoolean(5, true);
            createUser.setString(6, user.getAdress());
            createUser.setString(7, user.getRole());
            createUser.setString(8, user.getCpr());
            ResultSet userResult = createUser.executeQuery();
            User userReturn = null;
            while (userResult.next()) {
                userReturn = new User((UUID) userResult.getObject("id"), userResult.getString("username"), userResult.getString("email"), userResult.getBoolean("active"), userResult.getString("adress"), userResult.getString("role"), userResult.getString("cpr"));
            }
            if (user.getCitizensIDList() != null) {
                for (UUID citizenId : user.getCitizensIDList()) {
                    PreparedStatement insertIntoMyCitizens = this.connection.prepareStatement("INSERT INTO my_citizens VALUES (?, ?)");
                    insertIntoMyCitizens.setObject(1, userReturn.getId(), Types.OTHER);
                    insertIntoMyCitizens.setObject(2, citizenId, Types.OTHER);
                    insertIntoMyCitizens.execute();
                }
            }

            connection.commit();
            
            return userReturn;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User findUserById(UUID id) {
        try (PreparedStatement findUser = this.connection.prepareStatement("SELECT * FROM users WHERE id = ? AND active=true")) {
            findUser.setObject(1, id, Types.OTHER);
            ResultSet findUserResult = findUser.executeQuery();
            while (findUserResult.next()) {
                return new User((UUID) findUserResult.getObject("id"), findUserResult.getString("username"), findUserResult.getBoolean("active"), findUserResult.getString("role"), findUserResult.getString("email"), findUserResult.getString("adress"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User findUserByUsername(String username) {
        try (PreparedStatement findUser = this.connection.prepareStatement("SELECT *, (SELECT array(SELECT citizen_id FROM my_citizens WHERE my_citizens.user_id = users.id)) AS assignedCitizens FROM users WHERE username = ? AND active=true")) {
            findUser.setString(1, username);
            ResultSet findUsernameResult = findUser.executeQuery();
            while (findUsernameResult.next()) {
                Array arrayOfCitizens = findUsernameResult.getArray("assignedCitizens");
                List<UUID> myCitizens = Arrays.asList((UUID[])arrayOfCitizens.getArray());
                return new User((UUID) findUsernameResult.getObject("id"), findUsernameResult.getString("username"), findUsernameResult.getString("password"), findUsernameResult.getString("role"), findUsernameResult.getBoolean("active"), findUsernameResult.getString("email"), findUsernameResult.getString("cpr"), findUsernameResult.getString("adress"), myCitizens);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean deleteUserById(UUID id) {
        try (PreparedStatement deleteUser = this.connection.prepareStatement("UPDATE users SET active = false WHERE id = ? ")) {
            deleteUser.setObject(1, id, Types.OTHER);
            int affectedRows = deleteUser.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public User updateUser(User user) {
        try (PreparedStatement update = connection.prepareStatement("UPDATE users SET username = ?, password = ? , email = ? , adress = ?  WHERE id = ?  RETURNING id , username, password, email, active, adress, role, cpr")) {
            update.setString(1, user.getUsername());
            update.setString(2, user.getPassword());
            update.setString(3, user.getEmail());
            update.setString(4, user.getAdress());
            update.setObject(5, user.getId(), Types.OTHER);
            ResultSet updateResult = update.executeQuery();
            while (updateResult.next()) {
                return new User((UUID) updateResult.getObject("id"), updateResult.getString("username"), updateResult.getString("password"), updateResult.getString("email"), updateResult.getBoolean("active"), updateResult.getString("adress"), updateResult.getString("role"), updateResult.getString("cpr"), null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UUID> findCitizensById(UUID id) {
        List<UUID> citizens = new ArrayList<>();
        try (PreparedStatement findCitizen = this.connection.prepareStatement("SELECT * FROM my_citizens WHERE user_id = ?")) {
            findCitizen.setObject(1, id, Types.OTHER);
            ResultSet findCitizenResult = findCitizen.executeQuery();
            while (findCitizenResult.next()) {
                citizens.add((UUID) findCitizenResult.getObject("citizens_id"));
            }
            return citizens;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
