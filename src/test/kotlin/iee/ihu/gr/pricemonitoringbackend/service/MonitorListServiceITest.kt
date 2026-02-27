package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListCreateForm
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListUpdateForm
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.service.monitor.MonitorListService
import iee.ihu.gr.pricemonitoringbackend.service.subscription.SubscriptionService
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class MonitorListServiceITest(private val monitorListService: MonitorListService,private val subscriptionService: SubscriptionService) : AbstractITest() {

    private val rooms = mutableListOf<Room>()
    private val anotherHotelRooms = mutableListOf<Room>()
    private lateinit var createForm: MonitorListCreateForm
    private lateinit var updateForm: MonitorListUpdateForm
    private lateinit var hotel: Hotel
    private lateinit var anotherHotel: Hotel
    private lateinit var securityContextUser: User

    @BeforeEach
    fun prepareTestContext(){
        userRepository.findAll().shouldBeEmpty()
        hotel = createHotel()
        anotherHotel = createHotel()
        repeat(5){
            rooms.add(createRoom(hotel))
            anotherHotelRooms.add(createRoom(anotherHotel))
        }
        createForm = MonitorListCreateForm(rooms.mapNotNull(Room::id).toSet(),"test",setOf(1,2,3))
        updateForm = MonitorListUpdateForm(rooms.drop(2).mapNotNull(Room::id).plus(anotherHotelRooms.drop(1).mapNotNull(Room::id)).toSet(),"new_name",setOf(1,4,5),0)
        securityContextUser = createUser()

        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(securityContextUser.id!!,"",setOf(SimpleGrantedAuthority(Role.USER.name))) })
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should create new monitor list`() {
        monitorListRepository.findAllByOwner_Id(securityContextUser.id!!).shouldBeEmpty()

        monitorListService.createMonitorList(createForm)

        assertSoftly(monitorListRepository.findAllByOwner_Id(securityContextUser.id!!)) {
            shouldHaveSize(1)
            this[0].name shouldBe createForm.name
            this[0].owner.id shouldBe securityContextUser.id!!
            this[0].status shouldBe MonitorListStatus.ACTIVE
            this[0].rooms shouldContainExactlyInAnyOrder rooms
            this[0].distances shouldContainExactlyInAnyOrder createForm.distanceDays
        }
    }

    @Test
    fun `should throw DataIntegrityViolationException when trying to create monitor list with the same name for the same user`() {
        monitorListService.createMonitorList(createForm)

        shouldThrow<DataIntegrityViolationException> {
            monitorListService.createMonitorList(createForm)
        }
    }

    @Test
    fun `should not throw DataIntegrityViolationException when two users create monitor list with the same name`() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(securityContextUser.id,"") })
        monitorListService.createMonitorList(createForm)
        SecurityContextHolder.clearContext()

        val diffUserID = createUser().id!!
        diffUserID shouldNotBe securityContextUser.id!!
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(diffUserID,"") })
        monitorListService.createMonitorList(createForm)
        SecurityContextHolder.clearContext()

        assertSoftly(monitorListRepository.findAllByOwner_Id(diffUserID)) {
            shouldHaveSize(1)
            this[0].name shouldBe createForm.name
            this[0].owner.id shouldBe diffUserID
            this[0].status shouldBe MonitorListStatus.ACTIVE
            this[0].rooms shouldContainExactlyInAnyOrder rooms
        }
    }

    @Test
    fun `should find all monitor lists of user`() {
        monitorListService.monitorLists(securityContextUser.id!!).shouldBeEmpty()

        monitorListService.createMonitorList(createForm)

        assertSoftly(monitorListService.monitorLists(securityContextUser.id!!).apply { shouldHaveSize(1) }[0]) {
            name shouldBe createForm.name
            owner.id shouldBe securityContextUser.id!!
            status shouldBe MonitorListStatus.ACTIVE
            rooms shouldContainExactlyInAnyOrder rooms
            distances shouldContainExactlyInAnyOrder createForm.distanceDays
        }

    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to find monitor list by id that does not exist`() {
        shouldThrow<EmptyResultDataAccessException> { monitorListService.findMonitorList(1) }
    }

    @Test
    fun `should find monitor list by id`() {
        val monitorListID = monitorListService.createMonitorList(createForm).id!!

        assertSoftly(monitorListService.findMonitorList(monitorListID)) {
            name shouldBe createForm.name
            owner.id shouldBe securityContextUser.id!!
            status shouldBe MonitorListStatus.ACTIVE
            rooms shouldContainExactlyInAnyOrder rooms
            distances shouldContainExactlyInAnyOrder createForm.distanceDays
        }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to delete monitor list that does not exist`() {
        val idOfNotExistingMonitorList = 120390129L
        monitorListRepository.existsById(idOfNotExistingMonitorList).shouldBeFalse()

        shouldThrow<EmptyResultDataAccessException> { monitorListService.delete(idOfNotExistingMonitorList) }
    }

    @Test
    fun `should throw AccessDeniedException when user is trying to delete monitor list of another user`() {
        val monitorListID = monitorListService.createMonitorList(createForm).id!!

        val diffUserID = createUser().id!!
        diffUserID shouldNotBe securityContextUser.id!!
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(diffUserID,"") })

        shouldThrow<AccessDeniedException> { monitorListService.delete(monitorListID) }
    }

    @Test
    fun `should delete monitor list`() {
        val monitorList = monitorListService.createMonitorList(createForm.copy(rooms = createForm.rooms.plus(anotherHotelRooms.mapNotNull(Room::id))))

        monitorListService.delete(monitorList.id!!)

        monitorListRepository.existsById(monitorList.id!!).shouldBeFalse()
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to update monitor list that does not exist`() {
        val idOfNotExistingMonitorList = 120930129L
        monitorListRepository.existsById(idOfNotExistingMonitorList).shouldBeFalse()

        shouldThrow<EmptyResultDataAccessException> { monitorListService.update(updateForm.copy(id = idOfNotExistingMonitorList)) }
    }

    @Test
    fun `should throw AccessDeniedException when user is trying to update monitor list of another user`() {
        val monitorListID = monitorListService.createMonitorList(createForm).id!!

        val diffUserID = createUser().id!!
        diffUserID shouldNotBe securityContextUser.id!!
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(diffUserID,"",setOf(SimpleGrantedAuthority(Role.USER.name))) })

        shouldThrow<AccessDeniedException> { monitorListService.update(updateForm.copy(id = monitorListID)) }
    }

    @Test
    fun `should update monitor list`() {
        val monitorList = monitorListService.createMonitorList(createForm.copy(rooms = createForm.rooms.plus(anotherHotelRooms[0].id!!)))
        val thirdHotel = createHotel()
        val thirdHotelRooms = mutableListOf<Room>()
        repeat(5){ thirdHotelRooms.add(createRoom(thirdHotel)) }

        monitorListService.update(updateForm.copy(rooms = thirdHotelRooms.mapNotNull(Room::id).toSet(),id = monitorList.id!!))

        assertSoftly(monitorListRepository.findById(monitorList.id!!).get()) {
            name shouldBe updateForm.name
            rooms shouldContainExactlyInAnyOrder thirdHotelRooms
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    fun `should throw DataIntegrityViolationException when trying to update monitor list name and another list exists with the same name`() {
        try{
            val firstMonitorList = monitorListService.createMonitorList(createForm)
            val secondMonitorList = monitorListService.createMonitorList(createForm.copy(name = "another_name"))
            shouldThrow<DataIntegrityViolationException> { monitorListService.update(updateForm.copy(id = secondMonitorList.id!!, name = firstMonitorList.name)) }
        }finally {
            clear()
        }
    }

    @Test
    fun `should throw IncorrectResultSizeDataAccessException when trying to create monitor list and not all rooms are found`() {
        val idOfNotExistingRoom = 12093012930L
        roomRepository.existsById(idOfNotExistingRoom).shouldBeFalse()

        shouldThrow<IncorrectResultSizeDataAccessException> { monitorListService.createMonitorList(createForm.copy(rooms = createForm.rooms.plus(idOfNotExistingRoom))) }
    }

    @Test
    fun `should throw IncorrectResultSizeDataAccessException when trying to update monitor list and not all rooms are found`() {
        val monitorList = monitorListService.createMonitorList(createForm)
        val idOfNotExistingRoom = 12093012930L
        roomRepository.existsById(idOfNotExistingRoom).shouldBeFalse()

        shouldThrow<IncorrectResultSizeDataAccessException> { monitorListService.update(updateForm.copy(rooms = updateForm.rooms.plus(idOfNotExistingRoom),id = monitorList.id!!)) }
    }

    @MethodSource("blankStrings")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to create or update monitor list with blank name`(blankName: String) {
        val message1 = shouldThrow<ConstraintViolationException> { monitorListService.createMonitorList(createForm.copy(name = blankName)) }.constraintViolations.shouldHaveSize(1).iterator().next().message
        val message2 = shouldThrow<ConstraintViolationException> { monitorListService.update(updateForm.copy(name = blankName)) }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message1 shouldBe messageSource.getMessage("MonitorList.name.NotBlank.message")
        message1 shouldBe message2
    }

    @Test
    fun `should throw ConstraintViolationException when trying to create or update monitor list with empty rooms`() {
        val message1 = shouldThrow<ConstraintViolationException> { monitorListService.createMonitorList(createForm.copy(rooms = emptySet())) }.constraintViolations.shouldHaveSize(1).iterator().next().message
        val message2 = shouldThrow<ConstraintViolationException> { monitorListService.update(updateForm.copy(rooms = emptySet())) }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message1 shouldBe messageSource.getMessage("MonitorList.rooms.NotEmpty.message")
        message1 shouldBe message2
    }

    @Test
    fun `should throw ConstraintViolationException when trying to create or update monitor list with empty distances`() {
        val message1 = shouldThrow<ConstraintViolationException> { monitorListService.createMonitorList(createForm.copy(distanceDays = emptySet())) }.constraintViolations.shouldHaveSize(1).iterator().next().message
        val message2 = shouldThrow<ConstraintViolationException> { monitorListService.update(updateForm.copy(distanceDays = emptySet())) }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message1 shouldBe messageSource.getMessage("MonitorList.distanceDays.NotEmpty.message")
        message1 shouldBe message2
    }

    @Test
    fun `should return false when trying to check if not existing user is owner of not existing monitor list`() {
        val idOfNotExistingUser = 12031029L
        val idOfNotExistingMonitorList = 12090129L
        monitorListRepository.existsById(idOfNotExistingMonitorList).shouldBeFalse()
        userRepository.existsById(idOfNotExistingUser).shouldBeFalse()

        monitorListService.isOwner(idOfNotExistingMonitorList,idOfNotExistingUser).shouldBeFalse()
    }

    @Test
    fun `should return false when trying to check if not existing user is owner of monitor list`() {
        val idOfNotExistingUser = 12031029L
        val monitorListID = monitorListService.createMonitorList(createForm).id!!
        userRepository.existsById(idOfNotExistingUser).shouldBeFalse()

        monitorListService.isOwner(monitorListID,idOfNotExistingUser).shouldBeFalse()
    }

    @Test
    fun `should return false when trying to check if user is owner of not existing monitor list`() {
        val idOfNotExistingMonitorList = 12090129L
        monitorListRepository.existsById(idOfNotExistingMonitorList).shouldBeFalse()

        monitorListService.isOwner(idOfNotExistingMonitorList,securityContextUser.id!!)
    }


    @Test
    fun `should return if user is owner of monitor list`() {
        val notOwnerID = createUser().id!!
        val ownerID = securityContextUser.id!!
        val monitorListID = monitorListService.createMonitorList(createForm).id!!

        monitorListService.isOwner(monitorListID,notOwnerID).shouldBeFalse()
        monitorListService.isOwner(monitorListID,ownerID).shouldBeTrue()
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should throw IllegalArgumentException when maximum allowed rooms are exceeded`(role: Role) {
        changeUserRole(role)
        val subscriptionLimits = subscriptionService.subscriptionLimits(role)

        //return successfully when max rooms is empty which means unlimited number of rooms
        if(subscriptionLimits.maxRooms.isEmpty)
            return

        val rooms = mutableSetOf<Long>()
        repeat(subscriptionLimits.maxRooms.get()+1){ rooms.add(createRoom(hotel).id!!) }

        shouldThrow<IllegalArgumentException> { monitorListService.createMonitorList(createForm.copy(rooms = rooms)) }
        val monitorList = monitorListService.createMonitorList(createForm)
        shouldThrow<IllegalArgumentException> { monitorListService.update(updateForm.copy(rooms = rooms,id = monitorList.id!!)) }
        monitorListRepository.findAllByOwner_Id(securityContextUser.id!!).shouldContainExactlyInAnyOrder(monitorList)!![0].rooms.shouldHaveSize(createForm.rooms.size)
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should not throw IAE when maximum allowed rooms are not exceeded`(role: Role) {
        changeUserRole(role)
        val subscriptionLimits = subscriptionService.subscriptionLimits(role)

        if(subscriptionLimits.maxRooms.isEmpty)
            return

        val rooms = mutableSetOf<Long>()
        repeat(subscriptionLimits.maxRooms.get()){ rooms.add(createRoom(hotel).id!!) }


        val monitorList = monitorListService.createMonitorList(createForm.copy(rooms = rooms))
        monitorListService.update(updateForm.copy(rooms = rooms,id = monitorList.id!!))
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should throw AccessDeniedException when trying to create monitor list when maximum monitor list is exceeded`(role: Role) {
        changeUserRole(role)
        val subscriptionLimits = subscriptionService.subscriptionLimits(role)

        repeat(subscriptionLimits.maxMonitorList){ monitorListService.createMonitorList(createForm.copy(name = createForm.name.plus(it))) }

        shouldThrow<AccessDeniedException> { monitorListService.createMonitorList(createForm) }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should not throw AccessDeniedException when trying to create monitor list when maximum monitor list is not exceeded`(role: Role) {
        changeUserRole(role)
        val subscriptionLimits = subscriptionService.subscriptionLimits(role)

        repeat(subscriptionLimits.maxMonitorList - 1){ monitorListService.createMonitorList(createForm.copy(name = createForm.name.plus(it))) }

        monitorListService.createMonitorList(createForm)

        monitorListRepository.countUserMonitorList(securityContextUser.id!!).shouldBe(subscriptionLimits.maxMonitorList)
    }

    private fun clear(){
        monitorListRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun changeUserRole(role: Role){
        securityContextUser.role = role
        val currentAuthentication = SecurityContextHolder.getContext().authentication
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(currentAuthentication.principal,"",setOf(SimpleGrantedAuthority(role.name))) })
    }
}