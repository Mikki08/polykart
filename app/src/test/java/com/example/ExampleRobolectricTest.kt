package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.PolytechRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Политех Спутник", appName)
    }

    @Test
    fun `polytech repository loads buildings and services`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PolytechRepository(context)

        val buildings = repository.getCampusBuildings()
        assertTrue("Campus buildings should not be empty", buildings.isNotEmpty())

        val gk = buildings.firstOrNull { it.id == "GK" }
        assertNotNull("Main building GK should exist", gk)

        val services = repository.getPolyServices()
        assertTrue("Polytech services should not be empty", services.isNotEmpty())
    }

    @Test
    fun `smart search correctly finds chemistry teachers for mechatronics group 60002`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PolytechRepository(context)

        val query = "преподаватель химии у направления мехатроники и робототехники 1 курса группы 60002"
        val results = repository.smartSearch(query)

        assertEquals("Should return exactly 3 teachers for this query", 3, results.size)

        val names = results.map { it.fullName }
        assertTrue("Must include Полякова Вера Витальевна", names.contains("Полякова Вера Витальевна"))
        assertTrue("Must include Бочагина Татьяна Васильевна", names.contains("Бочагина Татьяна Васильевна"))
        assertTrue("Must include Крылов Андрей Сергеевич", names.contains("Крылов Андрей Сергеевич"))

        // Check specific roles
        val polyakova = results.first { it.fullName == "Полякова Вера Витальевна" }
        assertEquals("Лекция", polyakova.assignments.first().role)

        val bochagina = results.first { it.fullName == "Бочагина Татьяна Васильевна" }
        assertEquals("Практические занятия", bochagina.assignments.first().role)

        val krylov = results.first { it.fullName == "Крылов Андрей Сергеевич" }
        assertEquals("Лабораторный практикум", krylov.assignments.first().role)
    }

    @Test
    fun `student notes CRUD works`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PolytechRepository(context)

        val initialNotes = repository.getNotes()
        assertTrue("Should have initial notes", initialNotes.isNotEmpty())

        val updated = repository.addNote("Тестовая новая заметка")
        assertEquals("Should increase count by 1", initialNotes.size + 1, updated.size)
        assertEquals("Тестовая новая заметка", updated.first().text)
    }

    @Test
    fun `room daily schedule provides 6 bell slots`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PolytechRepository(context)

        val slots115 = repository.getRoomDailySchedule("ГК", "115")
        assertEquals(6, slots115.size)

        val pair1 = slots115.first()
        assertTrue("Pair 1 should be occupied in room 115", pair1.isOccupied)
        assertEquals("Химия", pair1.subject)
        assertEquals("Полякова В. В.", pair1.teacherName)
    }
}
