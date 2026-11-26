package ru.bank.paymentmicrocervice.services;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.bank.paymentmicrocervice.api.dto.CreateTransferByCardNumberRequest;
import ru.bank.paymentmicrocervice.api.dto.CreateTransferByPhoneNumberRequest;
import ru.bank.paymentmicrocervice.exceptions.AccessDeniedException;
import ru.bank.paymentmicrocervice.exceptions.FailedGrpcRequestException;
import ru.bank.paymentmicrocervice.exceptions.NoValidException;
import ru.bank.paymentmicrocervice.exceptions.NotFoundElementException;
import ru.bank.paymentmicrocervice.repositories.PaymentRepository;
import ru.bank.paymentmicrocervice.repositories.ProcessedEventRepository;
import ru.bank.proto.card.CardServiceGrpc;
import ru.bank.proto.card.GetCardInfoByCardNumberRequest;
import ru.bank.proto.card.GetCardInfoRequest;
import ru.bank.proto.card.GetCardInfoResponse;
import ru.bank.proto.payment.account.ClientPaymentAccountServiceGrpc;
import ru.bank.proto.payment.account.GetCardHolderRequest;
import ru.bank.proto.payment.account.GetCardHolderResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;

    @GrpcClient("card-service")
    private final CardServiceGrpc.CardServiceBlockingStub cardStub;

    @GrpcClient("account-service")
    private final ClientPaymentAccountServiceGrpc.ClientPaymentAccountServiceBlockingStub accountStub;

    public void createTransferByCardNumber(@Valid CreateTransferByCardNumberRequest request, UUID userId) {
        GetCardInfoResponse fromCardResponse;
        try {
            fromCardResponse = cardStub.getCardInfo(
                    GetCardInfoRequest.newBuilder()
                            .setId(request.getFromCardId().toString())
                            .build()
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new NotFoundElementException(
                        String.format("Карты с id=%s не найдено", request.getFromCardId())
                );
            }
            throw new FailedGrpcRequestException(
                    "Неизвестная ошибка во время обращения в сервис карт"
            );
        }


        if (!UUID.fromString(fromCardResponse.getUserId()).equals(userId)) {
            GetCardHolderResponse holderOfFromCard;
            try {
                //если он не является владельцем то проверить на владельца счета
                holderOfFromCard = accountStub.getCardHolder(
                        GetCardHolderRequest.newBuilder()
                                .setAccountId(fromCardResponse.getAccountId())
                                .build()
                );
            } catch (StatusRuntimeException e) {
                throw new FailedGrpcRequestException(
                        "Неизвестная ошибка во время обращения в сервис счетов"
                );
            }

            if (!UUID.fromString(holderOfFromCard.getUserId()).equals(userId)) {
                throw new AccessDeniedException("У вас не прав на эту карту");
            }
        }

        GetCardInfoResponse toCardResponse;
        try {
            toCardResponse = cardStub.getCardInfoByCardNumber(
                    GetCardInfoByCardNumberRequest.newBuilder()
                            .setCardNumber(request.getToCardNumber())
                            .build()
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                throw new NoValidException(
                        "Неправильный формат номера карты"
                );
            } else if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new NotFoundElementException(
                        String.format("Карты с номером toCardNumber=%s не существует",  request.getToCardNumber())
                );
            }
            throw new FailedGrpcRequestException(
                    "Неизвестная ошибка во время обращения в сервис карт"
            );
        }


        //сохраняем платеж в бд, а также событие кидаем в таблицу outbox

    }

    public void createTransferByPhoneNumber(@Valid CreateTransferByPhoneNumberRequest request, UUID userId) {

    }
}
