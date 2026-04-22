package com.green.eats.auth.application;

import com.green.eats.auth.application.model.UserPutReq;
import com.green.eats.auth.application.model.UserSigninReq;
import com.green.eats.auth.application.model.UserSignupReq;
import com.green.eats.auth.entity.User;
import com.green.eats.auth.exception.UserErrorCode;
import com.green.eats.common.constants.UserEventType;
import com.green.eats.common.exception.BusinessException;
import com.green.eats.common.model.UserEvent;
import com.green.eats.common.outbox.Outbox;
import com.green.eats.common.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    private void sendUserEvent(Long id, UserEvent userEvent){
        String payload = objectMapper.writeValueAsString(userEvent);
        Outbox outbox = Outbox.builder()
                .topic("user-topic")
                .aggregateId( userEvent.getUserId() )
                .eventType( userEvent.getEventType().name() )
                .payload( payload )
                .build();

        outboxRepository.save(outbox);

//        kafkaTemplate.send("user-topic", String.valueOf(id), userEvent)
//                .whenComplete((result, ex) -> {
//                    if (ex == null) {
//                        // 성공 시 로그
//                        log.info(" [Kafka Success] Topic: {}, Offset: {}",
//                                result.getRecordMetadata().topic(),
//                                result.getRecordMetadata().offset());
//                    } else {
//                        // 실패 시 로그
//                        log.error(" [Kafka Failure] 원인: {}", ex.getMessage());
//                    }
//                });

    }

    @Transactional
    public void signup(UserSignupReq req){
        String hashedPw = passwordEncoder.encode( req.getPassword() );

        //회원가입 시켜주세요
        User signedUser = new User();
        signedUser.setAddress(req.getAddress());
        signedUser.setName(req.getName());
        signedUser.setEmail(req.getEmail());
        signedUser.setPassword(hashedPw);
        signedUser.setEnumUserRole(req.getUserRole());
        signedUser.setIsDel(false);

        userRepository.save(signedUser);

        UserEvent userEvent = UserEvent.builder()
                .userId( signedUser.getId() )
                .name( signedUser.getName() )
                .eventType( UserEventType.USER_CREATED )
                .build();

        sendUserEvent(signedUser.getId(), userEvent);
    }

    @Transactional
    public void updUser(Long userId, UserPutReq req) {
        User user = userRepository.findById(userId).orElseThrow();

        user.setName( req.getName() );
        user.setAddress( req.getAddress() );

        userRepository.save(user);

        UserEvent userEvent = UserEvent.builder()
                .userId( user.getId() )
                .name( user.getName() )
                .eventType( UserEventType.USER_UPDATED )
                .build();

        sendUserEvent(user.getId(), userEvent);

    }

    public void delUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setIsDel( true );

        userRepository.save(user);

        UserEvent userEvent = UserEvent.builder()
                .userId( user.getId() )
                .name( user.getName() )
                .eventType( UserEventType.USER_DELETED )
                .build();

        sendUserEvent(user.getId(), userEvent);
    }



    public User signin(UserSigninReq req){
        User signedUser = userRepository.findByEmail(req.getEmail());
        log.info("signdUser {}", signedUser);
        if(signedUser == null || !passwordEncoder.matches(req.getPassword(), signedUser.getPassword())){
            notFoundUserAndNotMatchedPassword();
        }

        return signedUser;
    }

    private void notFoundUserAndNotMatchedPassword() {
        //throw new BusinessException(UserErrorCode.CHECK_EMAIL_PASSWORD);
        throw BusinessException.of(UserErrorCode.CHECK_EMAIL_PASSWORD);
    }
}
