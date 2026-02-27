package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.entity.Email
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.entity.User
import io.mockk.called
import io.mockk.every
import io.mockk.verifyAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.math.exp

class SubscriptionControllerUnitTest : AbstractControllerUnitTest() {

    private val user = User(Email("any@email.com"),"ANY_NAME","ANY_PASS", Role.values().random(),1)
    private val expectedJWT = "ANY_JWT"

    @BeforeEach
    fun subsClassSetUp() {
        every { subscriptionServiceMock.upgradeToBasic(user.id!!) } returns user
        every { subscriptionServiceMock.upgradeToPremium(user.id!!) } returns user
        every { jwtGeneratorMock.generate(user) } returns expectedJWT

    }

    @Test
    fun `should return 401(Unauthorized) when trying to upgrade subscription without being authenticated`() {
        mockMvc
            .post("/subscription/basic")
            .andExpect {
                status { isUnauthorized() }
            }

        mockMvc
            .post("/subscription/premium")
            .andExpect {
                status { isUnauthorized() }
            }

        verifyAll {
            subscriptionServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @MethodSource("allExceptPostMethodsWithRoles")
    @ParameterizedTest
    fun `should return 401(Unauthorized) when trying to upgrade subscription with invalid http method`(params: Pair<HttpMethod,Role>) {
        val (invalidHttpMethod,role) = params
        mockMvc
            .perform {
                request(invalidHttpMethod,"/subscription/basic")
                    .with(user("1").roles(role.name))
                    .buildRequest(it)
            }
            .andExpect(status().isUnauthorized)
        mockMvc
            .perform { request(invalidHttpMethod,"/subscription/premium").buildRequest(it) }
            .andExpect(status().isUnauthorized)

        verifyAll {
            subscriptionServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should upgrade user to basic subscription and return 200(OK) response with new jwt`(role: Role) {
        mockMvc
            .post("/subscription/basic"){
                with(user(user.id!!.toString()).roles(role.name))
            }.andExpect {
                status { isOk() }
                content { string(expectedJWT) }
            }

        verifyAll {
            subscriptionServiceMock.upgradeToBasic(user.id!!)
            jwtGeneratorMock.generate(user)
        }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should upgrade user to premium subscription and return 200(OK) response with new jwt`(role: Role) {
        mockMvc
            .post("/subscription/premium"){
                with(user(user.id!!.toString()).roles(role.name))
            }.andExpect {
                status { isOk() }
                content { string(expectedJWT) }
            }

        verifyAll {
            subscriptionServiceMock.upgradeToPremium(1)
            jwtGeneratorMock.generate(user)
        }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 400(Bad Request) if upgrade to basic subscription throws IAE`(role: Role) {
        every { subscriptionServiceMock.upgradeToBasic(user.id!!) } throws IllegalArgumentException("ANY_ERROR")

        mockMvc
            .post("/subscription/basic"){
                with(user(user.id!!.toString()).roles(role.name))
            }.andExpect {
                status { isBadRequest() }
                content { string("ANY_ERROR") }
            }

        verifyAll {
            subscriptionServiceMock.upgradeToBasic(user.id!!)
            jwtGeneratorMock wasNot called
        }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 400(Bad Request) if upgrade to premium subscription throws IAE`(role: Role) {
        every { subscriptionServiceMock.upgradeToPremium(user.id!!) } throws IllegalArgumentException("ANY_ERROR")

        mockMvc
            .post("/subscription/premium"){
                with(user(user.id!!.toString()).roles(role.name))
            }.andExpect {
                status { isBadRequest() }
                content { string("ANY_ERROR") }
            }

        verifyAll {
            subscriptionServiceMock.upgradeToPremium(user.id!!)
            jwtGeneratorMock wasNot called
        }
    }
}