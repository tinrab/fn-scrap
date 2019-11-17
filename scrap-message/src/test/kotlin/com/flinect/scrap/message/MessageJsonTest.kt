package com.flinect.scrap.message

import org.junit.Test
import kotlin.test.assertEquals

class MessageJsonTest {
    @MessageTypes(
        AddTodoAction::class
    )
    private interface Action

    @MessageTypeName("ADD_TODO")
    private data class AddTodoAction(
        @MessageMeta
        val requestId: String,
        val text: String,
        @MessageFieldSkip
        val skipped: Boolean,
        @MessageFieldName("x")
        val y: Int
    ) : Action

    private val actionJson = MessageJson.of(Action::class)

    @Test
    fun simple() {
        val todoAction = AddTodoAction("1", "go to sleep", true, 42)

        val json = actionJson.toJson(todoAction)
        assertEquals("{\"type\":\"ADD_TODO\",\"payload\":{\"text\":\"go to sleep\",\"x\":42},\"meta\":{\"requestId\":\"1\"}}", json)

        val parsed = actionJson.fromJson(json) as AddTodoAction
        assertEquals(todoAction.requestId, parsed.requestId)
        assertEquals(todoAction.text, parsed.text)
        assertEquals(todoAction.y, parsed.y)
    }

    @Test
    fun extraField() {
        val parsed = actionJson.fromJson("{\"type\":\"ADD_TODO\",\"payload\":{\"text\":\"go to sleep\",\"x\":42,\"y\":13},\"meta\":{\"requestId\":\"1\",\"clientId\":\"1\"}}") as AddTodoAction
        assertEquals("1", parsed.requestId)
        assertEquals("go to sleep", parsed.text)
        assertEquals(42, parsed.y)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonObject() {
        actionJson.fromJson("42")
    }

    @Test(expected = IllegalArgumentException::class)
    fun typeNotRegistered() {
        data class A(
            val x: Int
        ) : Action
        actionJson.toJson(A(42))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noMessageTypeAnnotation() {
        data class A(
            val x: Int
        ) : Action
        actionJson.registerMessageType(A::class)
        println(actionJson.toJson(A(42)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownType() {
        actionJson.fromJson("{\"type\":\"A\"}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingPayload() {
        actionJson.fromJson("{\"type\":\"ADD_TODO\"}")
    }
}
