package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.UserDto;
import com.upc.webworksbackend.exception.ForbiddenException;
import com.upc.webworksbackend.exception.NotFoundException;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto addUser(UserDto userDto) {
        UserModel userModel = userRepository.findByUsername(userDto.getUsername());
        if (userModel == null) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(userDto.getPassword());
            userDto.setPassword(encodedPassword);
            ModelMapper modelMapper = new ModelMapper();
            userModel = modelMapper.map(userDto, UserModel.class);
            userModel = userRepository.save(userModel);
            userDto = modelMapper.map(userModel, UserDto.class);
            return userDto;
        } else {
            return null;
        }
    }

    public UserDto userByUsername(String username) {
        UserModel user = userRepository.findByUsername(username);
        if (user != null) {
            ModelMapper modelMapper = new ModelMapper();
            return modelMapper.map(user, UserDto.class);
        }
        throw new NotFoundException("Usuario no encontrado con username: " + username);
    }

    public Boolean updateUser(UserDto userDto) {
        UserModel userModel = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + userDto.getId()));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (userDto.getCurrentPassword() == null || !passwordEncoder.matches(userDto.getCurrentPassword(), userModel.getPassword())) {
            throw new ForbiddenException("La contraseña actual es incorrecta.");
        }

        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(userDto.getPassword());
            userModel.setPassword(encodedPassword);
        }

        userModel.setName(userDto.getName());
        userModel.setLastname(userDto.getLastname());
        userModel.setBirthDate(userDto.getBirthDate());
        userModel.setPhone(userDto.getPhone());
        userModel.setEmail(userDto.getEmail());
        userModel.setUsername(userDto.getUsername());
        if (userDto.getPhoto() != null) {
            userModel.setPhoto(userDto.getPhoto());
        }
        // El rol NO debe poder cambiarse por esta via
        // userModel.getRol() se mantiene intacto

        userRepository.save(userModel);
        return true;
    }

    public Boolean deleteUser(Integer id) {
        UserModel userModel = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
        userRepository.delete(userModel);
        return true;
    }

    public UserDto getUserById(Integer id) {
        UserModel userModel = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(userModel, UserDto.class);
    }

    public List<UserDto> getAllUsers() {
        List<UserModel> userModels = userRepository.findAll();
        ModelMapper modelMapper = new ModelMapper();
        return Arrays.asList(modelMapper.map(userModels, UserDto[].class));
    }
}
