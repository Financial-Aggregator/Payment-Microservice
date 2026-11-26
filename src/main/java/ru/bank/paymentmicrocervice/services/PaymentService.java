package ru.bank.paymentmicrocervice.services;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.bank.cardmicroservice.proto.account.AccountServiceGrpc;
import ru.bank.cardmicroservice.proto.account.GetCardHolderRequest;
import ru.bank.cardmicroservice.proto.account.GetCardHolderResponse;
import ru.bank.cardmicroservice.proto.user.GetUserIdByPhoneNumberRequest;
import ru.bank.cardmicroservice.proto.user.GetUserIdByPhoneNumberResponse;
import ru.bank.cardmicroservice.proto.user.UserServiceGrpc;
import ru.bank.paymentmicrocervice.api.dto.CreateTransferByCardNumberRequest;
import ru.bank.paymentmicrocervice.api.dto.CreateTransferByPhoneNumberRequest;
import ru.bank.paymentmicrocervice.exceptions.AccessDeniedException;
import ru.bank.paymentmicrocervice.exceptions.FailedGrpcRequestException;
import ru.bank.paymentmicrocervice.exceptions.NoValidException;
import ru.bank.paymentmicrocervice.exceptions.NotFoundElementException;
import ru.bank.paymentmicrocervice.repositories.PaymentRepository;
import ru.bank.paymentmicrocervice.repositories.ProcessedEventRepository;
import ru.bank.proto.card.*;

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
    private final AccountServiceGrpc.AccountServiceBlockingStub accountStub;

    @GrpcClient("user-service")
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    private void validateAccessToCard(UUID cardId, UUID checkUserId) {
        GetCardInfoResponse fromCardResponse;
        try {
            fromCardResponse = cardStub.getCardInfo(
                    GetCardInfoRequest.newBuilder()
                            .setId(cardId.toString())
                            .build()
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new NotFoundElementException(
                        String.format("Карты с id=%s не найдено", cardId)
                );
            }
            throw new FailedGrpcRequestException(
                    "Неизвестная ошибка во время обращения в сервис карт"
            );
        }


        if (!UUID.fromString(fromCardResponse.getUserId()).equals(checkUserId)) {
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

            if (!UUID.fromString(holderOfFromCard.getUserId()).equals(checkUserId)) {
                throw new AccessDeniedException("У вас не прав на эту карту");
            }
        }
    }

    private void persistContext() {
        /*TODO: добавить логику сохранения в платежа и события в бд
          Должен создаваться перевод в собственной бд, а также же в таблице outbox событие о инициализации перевода.
        */
    }

    public void createTransferByCardNumber(@Valid CreateTransferByCardNumberRequest request, UUID userId) {
        //проверяем имеет ли пользователь доступ к карте
        validateAccessToCard(request.getFromCardId(), userId);

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

        //сохраняем платеж и событие в бд
        persistContext();
    }

    public void createTransferByPhoneNumber(@Valid CreateTransferByPhoneNumberRequest request, UUID userId) {
        //проверяем имеет ли пользователь доступ к карте
        validateAccessToCard(request.getFromCardId(), userId);

        GetUserIdByPhoneNumberResponse user;
        try {
            user = userStub.getUserIdByPhoneNumber(
                    GetUserIdByPhoneNumberRequest.newBuilder()
                            .setPhoneNumber(request.getToPhoneNumber())
                            .build()
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new NotFoundElementException(
                        String.format("Номер %s не зарегистрирован",  request.getToPhoneNumber())
                );
            }
            throw new FailedGrpcRequestException(
                    "Неизвестная ошибка во время обращения в сервис пользователей"
            );
        }

        GetCardInfoResponse toCardResponse;
        try {
            toCardResponse = cardStub.getPrimaryCardInfoByUserId(
                    GetPrimaryCardInfoByUserIdRequest.newBuilder()
                            .setUserId(user.getUserId())
                            .build()
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new NotFoundElementException(
                        String.format("У пользователя с userId=%s нет карт",  user.getUserId())
                );
            }
            throw new FailedGrpcRequestException(
                    "Неизвестная ошибка во время обращения в сервис карт"
            );
        }

        //сохраняем платеж и событие в бд
        persistContext();
    }
}
