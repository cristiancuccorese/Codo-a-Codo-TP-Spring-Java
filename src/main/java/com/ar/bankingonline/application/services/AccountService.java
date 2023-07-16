package com.ar.bankingonline.application.services;

import com.ar.bankingonline.api.dtos.AccountDto;
import com.ar.bankingonline.api.mappers.AccountMapper;
import com.ar.bankingonline.domain.exceptions.AccountNotFoundException;
import com.ar.bankingonline.domain.models.Account;
import com.ar.bankingonline.domain.models.User;
import com.ar.bankingonline.insfrastructure.repositories.AccountRepository;
import com.ar.bankingonline.insfrastructure.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
public class AccountService {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private UserRepository userRepository;

    //Obtiene la lista de cuentas bancarias y devuelve Lista de objetos AccountDto que representan las cuentas.

    @Transactional
    public List<AccountDto> getAccounts() {
        List<Account> accounts = repository.findAll();
        return accounts.stream().map(AccountMapper::AccountToDto).toList();
    }

    //Crea una nueva cuenta bancaria asociada a un usuario.
    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        Optional<User> user = userRepository.findById(accountDto.getOwner().getId());
        Account accountModel = AccountMapper.dtoToAccount(accountDto);
        accountModel.setOwner(user.orElseThrow(() -> new AccountNotFoundException("User not found with id: " + accountDto.getOwner().getId())));
        accountModel = repository.save(accountModel);
        return AccountMapper.AccountToDto(accountModel);
    }

    // Obtiene una cuenta bancaria por su ID.

    @Transactional
    public AccountDto getAccountById(Long id) {
        Account account = repository.findById(id).orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + id));
        return AccountMapper.AccountToDto(account);
    }

    //Actualiza la información de una cuenta bancaria existente.

    @Transactional
    public AccountDto updateAccount(Long id, AccountDto accountDto) {
        Account account = repository.findById(id).orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + id));

        // Validar y actualizar solo los campos relevantes
        if (accountDto.getAmount() != null && accountDto.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            account.setBalance(accountDto.getAmount());
        }

        if (accountDto.getOwner() != null) {
            User user = userRepository.getReferenceById(accountDto.getOwner().getId());
            account.setOwner(user);
        }

        Account savedAccount = repository.save(account);
        return AccountMapper.AccountToDto(savedAccount);
    }

    // * Elimina una cuenta bancaria por su ID.


    @Transactional
    public String deleteAccount(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return "Se ha eliminado la cuenta";
        } else {
            return "No se ha eliminado la cuenta";
        }
    }


    @Transactional
    public BigDecimal withdraw(BigDecimal amount, Long idOrigin) {
        return performTransaction(amount.negate(), idOrigin);
    }

    @Transactional
    public BigDecimal addAmountToAccount(BigDecimal amount, Long idOrigin) {
        return performTransaction(amount, idOrigin);
    }

    private BigDecimal performTransaction(BigDecimal amount, Long idOrigin) {
        Account account = repository.findById(idOrigin).orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + idOrigin));
        //verifico si  hay fondos suficientes
        if (account.getBalance().add(amount).compareTo(BigDecimal.ZERO) >= 0) {
            //actualizo el saldo de lacuenta con el nuevo monto
            account.setBalance(account.getBalance().add(amount));
            //guardo cambios en la DB
            repository.save(account);
        }

        return account.getBalance();
    }
}
