package com.itt.service.service;

//Service Layer Example
/*
@Service
@RequiredArgsConstructor
public class UserService {
 
 private final UserRepository userRepository;
 
 public UserDto createUser(CreateUserRequest request) {
     // Check if user already exists
     if (userRepository.existsByEmail(request.getEmail())) {
         throw ExceptionFactory.emailAlreadyRegistered();
     }
     
     // Business logic...
     User user = new User();
     // ... mapping logic
     
     User savedUser = userRepository.save(user);
     return mapToDto(savedUser);
 }
 
 public UserDto findById(Long id) {
     User user = userRepository.findById(id)
             .orElseThrow(() -> ExceptionFactory.userNotFound());
     return mapToDto(user);
 }
 
 public void deleteUser(Long id) {
     if (!userRepository.existsById(id)) {
         throw ExceptionFactory.userNotFound();
     }
     userRepository.deleteById(id);
 }
}
*/
