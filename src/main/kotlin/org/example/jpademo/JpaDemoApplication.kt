package org.example.jpademo

import jakarta.persistence.*
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@SpringBootApplication
class JpaDemoApplication

fun main(args: Array<String>) {
    runApplication<JpaDemoApplication>(*args)
}

@Component
class TestRunner(val testService: TestService) : CommandLineRunner {
    override fun run(vararg args: String) {
        testService.init()
        testService.showChild() //kakao group: kakao games, kakao bank
        testService.`카카오 뱅크가 있는 단체에 카카오페이 추가`()
        testService.showChild() //kakao group: kakao bank, kakao pay
        // kako games is missing?!
    }
}

@Service
class TestService(val orgRepository: GroupRepository) {

    @Transactional
    fun init() {
        orgRepository.save(Org(name = "kakao group").apply {
            children.add(Child(name = "kakao games"))
            children.add(Child(name = "kakao bank"))
        })
    }

    @Transactional
    fun `카카오 뱅크가 있는 단체에 카카오페이 추가`() {
        val group = orgRepository.findOrgByChildName("kakao bank").apply {
            children.add(Child(name = "kakao pay"))
        }
        orgRepository.save(group)
    }

    @Transactional
    fun showChild() {
        for (org in orgRepository.findAll()) {
            println(org.name + ": " + org.children.joinToString { it.name })
        }
    }
}

interface GroupRepository : JpaRepository<Org, Long> {
    @Query("select o from Org o join fetch o.children c where c.name like concat('%', :name, '%')")
    fun findOrgByChildName(name: String): Org
}

@Entity
data class Org(
    @Id @GeneratedValue val id: Long? = null,
    val name: String,
    @OneToMany(cascade = [CascadeType.ALL]) val children: MutableList<Child> = mutableListOf())

@Entity
data class Child(
    @Id @GeneratedValue val id: Long? = null,
    val name: String)

