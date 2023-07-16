package com.ar.bankingonline.application.services;

import com.ar.bankingonline.api.dtos.AccountDto;
import com.ar.bankingonline.api.dtos.UserDto;
import com.ar.bankingonline.api.mappers.AccountMapper;
import com.ar.bankingonline.api.mappers.UserMapper;
import com.ar.bankingonline.domain.exceptions.AccountNotFoundException;
import com.ar.bankingonline.domain.models.Account;
import com.ar.bankingonline.domain.models.User;
import com.ar.bankingonline.insfrastructure.repositories.AccountRepository;
import com.ar.bankingonline.insfrastructure.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public UserService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public List<UserDto> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::userMapToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("User not found with id: " + id));
        return UserMapper.userMapToDto(user);
    }

    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.dtoToUser(userDto);
        user = userRepository.save(user);
        return UserMapper.userMapToDto(user);
    }

    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("User not found with id: " + id));

        existingUser.setUsername(userDto.getUsername());
        existingUser.setPassword(userDto.getPassword());

        if (userDto.getIdAccounts() != null) {
            List<Account> accountsToAdd = accountRepository.findAllById(userDto.getIdAccounts());
            existingUser.getAccounts().clear();
            existingUser.getAccounts().addAll(accountsToAdd);
        } else {
            existingUser.getAccounts().clear();
        }

        return UserMapper.userMapToDto(existingUser);
    }

    public String delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "Se ha eliminado el usuario";
        } else {
            return "No se ha eliminado el usuario";
        }
    }

    public UserDto addAccountToUser(AccountDto accountDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AccountNotFoundException("User not found with id: " + userId));
        Account account = AccountMapper.dtoToAccount(accountDto);

        if (account != null) {
            account.setOwner(user);
            user.getAccounts().add(account);

            userRepository.save(user);

            return UserMapper.userMapToDto(user);
        } else {
            throw new AccountNotFoundException("Account is null");
        }
    }
}

