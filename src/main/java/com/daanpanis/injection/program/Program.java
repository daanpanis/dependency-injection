package com.daanpanis.injection.program;

import com.daanpanis.core.api.command.Command;
import com.daanpanis.core.api.command.CommandManager;
import com.daanpanis.core.api.command.Message;
import com.daanpanis.core.api.command.exceptions.CommandExecutionException;
import com.daanpanis.core.api.command.parsers.IntegerParser;
import com.daanpanis.core.api.command.parsers.StringParser;
import com.daanpanis.core.api.command.permission.Permission;
import com.daanpanis.core.command.CoreCommandManager;
import com.daanpanis.core.program.Debugger;
import com.daanpanis.core.program.TestCommandSender;
import com.daanpanis.injection.DependencyInjector;
import com.daanpanis.injection.Inject;
import com.daanpanis.injection.impl.ServiceInjector;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Program {

    public static final CommandSender TEST_SENDER = new TestCommandSender();

    public static void main(String[] args) throws Exception {
        Debugger.debug = false;
        DependencyInjector injector = new ServiceInjector();
        injector.addSingleton(DatabaseService.class, DatabaseServiceImpl.class);
        injector.addScoped(UserService.class, UserServiceImpl.class);

        CommandManager commandManager = new CoreCommandManager();
        commandManager.registerParameterType(int.class, new IntegerParser());
        commandManager.registerParameterType(String.class, new StringParser());
        commandManager.registerCommands(injector.inject(UserCommands.class));

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String line;
            while ((line = scanner.nextLine()) != null) {
                try {
                    commandManager.executeCommand(TEST_SENDER, line);
                } catch (CommandExecutionException ex) {
                    System.err.println(ex.getMessage());
                }
            }
            scanner.close();

        }).start();
    }

    public static class UserCommands {

        @Inject(castFrom = UserService.class)
        private UserServiceImpl userService;

        @Command(syntax = "user add {1} {2} {3}")
        @Permission(node = "kappa")
        void addUser(CommandSender sender, int id, int age, @Message String name) {
            userService.addUser(id, name, age);
            sender.sendMessage("Added new user with id: " + id);
        }

        @Command(syntax = "user get {1}")
        void getUser(CommandSender sender, int id) {
            User user = userService.getUser(id);
            if (user != null) { sender.sendMessage(user.toString()); } else sender.sendMessage("No user found by this ID!");
        }

    }

    public static class DatabaseServiceImpl implements DatabaseService {

        private final Map<String, List<Object[]>> tables = new HashMap<>();

        @Override
        public List<Object[]> query(String table, Predicate<Object[]> matcher) {
            if (tables.containsKey(table)) {
                return tables.get(table).stream().filter(matcher).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public void addRow(String table, Object[] row) {
            tables.computeIfAbsent(table, f -> new ArrayList<>()).add(row);
        }
    }

    public static class UserServiceImpl implements UserService {

        private final DatabaseService database;

        public UserServiceImpl(DatabaseService database) {
            this.database = database;
        }

        @Override
        public User getUser(int id) {
            List<User> users = database.query("users", objects -> objects[0].equals(id)).stream()
                    .map(objects -> new User((int) objects[0], (String) objects[1], (int) objects[2])).collect(Collectors.toList());
            return !users.isEmpty() ? users.get(0) : null;
        }

        @Override
        public void addUser(int id, String name, int age) {
            database.addRow("users", new Object[]{id, name, age});
        }
    }

}
