package com.itt.service.fw.ratelimit.service;

import com.itt.service.entity.User;
import com.itt.service.entity.UserPlanMapping;
import com.itt.service.fw.ratelimit.dto.request.UserCreationRequestDto;
import com.itt.service.fw.ratelimit.dto.request.UserLoginRequestDto;
import com.itt.service.fw.ratelimit.dto.response.TokenSuccessResponseDto;
import com.itt.service.fw.ratelimit.dto.response.UserResponseDto;
import com.itt.service.exception.AccountAlreadyExistsException;
import com.itt.service.exception.InvalidLoginCredentialsException;
import com.itt.service.exception.InvalidPlanException;
import com.itt.service.fw.ratelimit.utility.JwtUtility;
import com.itt.service.fw.ratelimit.utility.UserResponseMapper;
import com.itt.service.repository.PlanRepository;
import com.itt.service.repository.UserPlanMappingRepository;
import com.itt.service.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

	private final JwtUtility jwtUtility;
	private final UserRepository userRepository;
	private final PlanRepository planRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserPlanMappingRepository userPlanMappingRepository;
	private final UserResponseMapper userResponseMapper;

	/**
	 * Creates a new user account in the system corresponding to provided
	 * subscription plan in the request.
	 *
	 * @param userCreationRequest containing user account details.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @throws AccountAlreadyExistsException If an account with the provided email-id already exists.
	 * @throws InvalidPlanException If the provided plan ID is invalid.
	 */
	@Transactional
	public void create(@NonNull final UserCreationRequestDto userCreationRequest) {
		final var emailId = userCreationRequest.getEmailId();
		final var userAccountExistsWithEmailId = userRepository.existsByEmailId(emailId);
		if (Boolean.TRUE.equals(userAccountExistsWithEmailId)) {
			throw new AccountAlreadyExistsException("Account with provided email-id already exists");
		}

		final var planId = userCreationRequest.getPlanId();
		final var isPlanIdValid = planRepository.existsById(planId);
		if (Boolean.FALSE.equals(isPlanIdValid)) {
			throw new InvalidPlanException("No plan exists in the system with provided-id");
		}

		final var user = new User();
		final var encodedPassword = passwordEncoder.encode(userCreationRequest.getPassword());
		user.setEmailId(emailId);
		user.setPassword(encodedPassword);
		final var savedUser = userRepository.save(user);

		final var userPlanMapping = new UserPlanMapping();
		userPlanMapping.setUserId(savedUser.getId());
		userPlanMapping.setPlanId(planId);
		userPlanMappingRepository.save(userPlanMapping);
	}

	/**
	 * Validates user login credentials and generates an access token on successful
	 * authentication.
	 *
	 * @param userLoginRequest The request object containing user login credentials.
	 * @return The access token response containing the generated access token.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @throws InvalidLoginCredentialsException If the provided login credentials are invalid.
	 */
	public TokenSuccessResponseDto login(@NonNull final UserLoginRequestDto userLoginRequest) {
		final var user = userRepository.findByEmailId(userLoginRequest.getEmailId())
				.orElseThrow(InvalidLoginCredentialsException::new);

		final var encodedPassword = user.getPassword();
		final var plainTextPassword = userLoginRequest.getPassword();
		final var isCorrectPassword = passwordEncoder.matches(plainTextPassword, encodedPassword);
		if (Boolean.FALSE.equals(isCorrectPassword)) {
			throw new InvalidLoginCredentialsException();
		}

		final var accessToken = jwtUtility.generateAccessToken(user.getId());
		return TokenSuccessResponseDto.builder().accessToken(accessToken).build();
	}

	public List<UserResponseDto> retrieve() {
		List<User> users = userRepository.findAll();

		Map<UUID, String> userIdToPlanMap = fetchUserPlanNames(users);

		return users.stream()
				.map(user -> userResponseMapper.toDto(user, userIdToPlanMap.get(user.getId())))
				.toList();
	}

	private Map<UUID, String> fetchUserPlanNames(List<User> users) {
		List<UUID> userIds = users.stream().map(User::getId).toList();

		return userPlanMappingRepository.findAllByUserIdInAndIsActiveTrue(userIds)
				.stream()
				.collect(Collectors.toMap(
						UserPlanMapping::getUserId,
						mapping -> mapping.getPlan().getName(),
						(existing, replacement) -> existing
				));
	}
}