package com.kt.kol.api.user.service

import com.kt.kol.api.user.model.User
import com.kt.kol.api.user.model.UserDTO
import com.kt.kol.api.user.repository.UserRepository
import com.kt.kol.common.exception.BusinessException
import com.kt.kol.common.model.PageDTO
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger(UserService::class.java)

@Service
class UserService(
    private val userRepository: UserRepository
) {

    suspend fun findAllUsersPaged(page: Int, size: Int): PageDTO<UserDTO> {
        val offset = page.toLong() * size

        val totalElements = userRepository.countAll()
        val content = userRepository.findPage(offset, size)
            .map { toDTO(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    suspend fun findUserById(id: Long): UserDTO {
        val user = userRepository.findById(id)
            ?: throw BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        return toDTO(user)
    }

    suspend fun findUserByUsername(username: String): UserDTO {
        val user = userRepository.findByUsername(username)
            ?: throw BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        return toDTO(user)
    }

    @Transactional
    suspend fun createUser(userDTO: UserDTO): UserDTO {
        val usernameExists = userRepository.existsByUsername(userDTO.username!!)
        if (usernameExists) {
            throw BusinessException("USER_EXISTS", "이미 존재하는 사용자명입니다.")
        }

        val emailExists = userRepository.existsByEmail(userDTO.email!!)
        if (emailExists) {
            throw BusinessException("EMAIL_EXISTS", "이미 존재하는 이메일입니다.")
        }

        val user = toEntity(userDTO).apply {
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
            active = true
        }

        val savedUser = userRepository.save(user)
        val result = toDTO(savedUser)
        log.info("User created: ${result.username}")
        return result
    }

    @Transactional
    suspend fun updateUser(id: Long, userDTO: UserDTO): UserDTO {
        val existingUser = userRepository.findById(id)
            ?: throw BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")

        existingUser.apply {
            firstName = userDTO.firstName
            lastName = userDTO.lastName
            email = userDTO.email
            active = userDTO.active
            updatedAt = LocalDateTime.now()
        }

        val savedUser = userRepository.save(existingUser)
        val result = toDTO(savedUser)
        log.info("User updated: ${result.username}")
        return result
    }

    @Transactional
    suspend fun deleteUser(id: Long) {
        val user = userRepository.findById(id)
            ?: throw BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")

        userRepository.delete(user)
        log.info("User deleted: $id")
    }

    suspend fun searchUsersPaged(query: String, page: Int, size: Int): PageDTO<UserDTO> {
        val offset = page.toLong() * size

        val totalElements = userRepository.countByNameSearch(query)
        val content = userRepository.searchByNamePage(query, offset, size)
            .map { toDTO(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    private fun toDTO(user: User): UserDTO {
        return UserDTO(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            active = user.active,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    private fun toEntity(dto: UserDTO): User {
        return User(
            username = dto.username,
            email = dto.email,
            firstName = dto.firstName,
            lastName = dto.lastName,
            active = dto.active
        )
    }
}