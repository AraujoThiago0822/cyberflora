package com.cyberflora.backend.security

import com.cyberflora.backend.repository.UserRepository
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("Usuário não encontrado: $email") }

        return SpringUser.builder()
            .username(user.email)
            .password(user.password)
            .authorities(emptyList())
            .build()
    }
}