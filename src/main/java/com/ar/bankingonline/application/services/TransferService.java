package com.ar.bankingonline.application.services;

import com.ar.bankingonline.api.dtos.TransferDto;
import com.ar.bankingonline.api.mappers.TransferMapper;
import com.ar.bankingonline.application.exceptions.InsufficientFundsException;
import com.ar.bankingonline.domain.exceptions.AccountNotFoundException;
import com.ar.bankingonline.domain.exceptions.TransferNotFoundException;
import com.ar.bankingonline.domain.models.Account;
import com.ar.bankingonline.domain.models.Transfer;
import com.ar.bankingonline.insfrastructure.repositories.AccountRepository;
import com.ar.bankingonline.insfrastructure.repositories.TransfersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService {

    @Autowired
    private TransfersRepository repository;

    @Autowired
    private AccountRepository accountRepository;

    public TransferService(TransfersRepository repository) {
        this.repository = repository;
    }


    public List<TransferDto> getTransfers() {
        List<Transfer> transfers = repository.findAll();
        return transfers.stream().map(TransferMapper::transferToDto).collect(Collectors.toList());
    }

    public TransferDto getTransferById(Long id) {
        Transfer transfer = repository.findById(id).orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + id));
        return TransferMapper.transferToDto(transfer);
    }

    public TransferDto updateTransfer(Long id, TransferDto transferDto) {
        Transfer transfer = repository.findById(id).orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + id));
        Transfer updatedTransfer = TransferMapper.dtoToTransfer(transferDto);
        updatedTransfer.setId(transfer.getId());
        return TransferMapper.transferToDto(repository.save(updatedTransfer));
    }

    public String deleteTransfer(Long id) {
        Transfer transfer = repository.findById(id).orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + id));
        Account originAccount = getAccountById(transfer.getOrigin());
        Account destinationAccount = getAccountById(transfer.getTarget());
        BigDecimal transferAmount = transfer.getAmount();

        // Restaurar fondos a la cuenta de origen
        originAccount.setBalance(originAccount.getBalance().add(transferAmount));
        saveAccount(originAccount);

        // Restar el monto transferido de la cuenta destino
        destinationAccount.setBalance(destinationAccount.getBalance().subtract(transferAmount));
        saveAccount(destinationAccount);

        repository.deleteById(id);
        return "Se ha eliminado la transferencia y los fondos han sido devueltos a la cuenta de origen";
    }

    @Transactional
    public TransferDto performTransfer(TransferDto dto) {
        // Validar datos del DTO
        validateTransferDto(dto);

        // Comprobar si la cuenta de origen existe
        Account originAccount = getAccountById(dto.getOrigin());

        // Comprobar si la cuenta de destino existe
        Account destinationAccount = getAccountById(dto.getTarget());

        // Comprobar si la cuenta de origen tiene fondos suficientes
        BigDecimal transferAmount = dto.getAmount();
        if (originAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in the account with id: " + dto.getOrigin());
        }

        // Realizar la transferencia
        originAccount.setBalance(originAccount.getBalance().subtract(transferAmount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(transferAmount));

        // Guardar las cuentas actualizadas
        saveAccount(originAccount);
        saveAccount(destinationAccount);

        // Crear la transferencia y guardarla en la base de datos
        Transfer transfer = createTransfer(dto);

        // Devolver el DTO de la transferencia realizada
        return TransferMapper.transferToDto(transfer);
    }

    private void validateTransferDto(TransferDto dto) {
        if (dto == null || dto.getOrigin() == null || dto.getTarget() == null || dto.getAmount() == null) {
            throw new AccountNotFoundException("Cuenta No encontrada");
        }
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferNotFoundException("El monto debe ser mayor a 0");
        }

    }

    private Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }

    private void saveAccount(Account account) {
        accountRepository.save(account);
    }

    private Transfer createTransfer(TransferDto dto) {
        Transfer transfer = new Transfer();
        Date date = new Date();
        transfer.setDate(date);
        transfer.setOrigin(dto.getOrigin());
        transfer.setTarget(dto.getTarget());
        transfer.setAmount(dto.getAmount());
        return repository.save(transfer);
    }


}