package com.flinect.scrap.message

import org.junit.Test
import kotlin.test.assertEquals

class MessageSerializerTest {
    @MessageTypes(
        AddTodoAction::class
    )
    private interface Action : Message

    @MessageTypeName("ADD_TODO")
    private data class AddTodoAction(
        val text: String,
        @MessageFieldSkip
        val skipped: Boolean,
        @MessageFieldName("x")
        val y: Int
    ) : Action

    private val actionJson = MessageSerializer.of(Action::class)

    @Test
    fun simple() {
        val todoAction = AddTodoAction("go to sleep", true, 42)

        val json = actionJson.encode(todoAction)
        assertEquals(
            "{\"type\":\"ADD_TODO\",\"payload\":{\"text\":\"go to sleep\",\"x\":42}}",
            json
        )

        val parsed = actionJson.decode(json) as AddTodoAction
        assertEquals(todoAction.text, parsed.text)
        assertEquals(todoAction.y, parsed.y)
    }

    @Test
    fun extraField() {
        val parsed =
            actionJson.decode("{\"type\":\"ADD_TODO\",\"payload\":{\"text\":\"go to sleep\",\"x\":42,\"y\":13}}") as AddTodoAction
        assertEquals("go to sleep", parsed.text)
        assertEquals(42, parsed.y)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonObject() {
        actionJson.decode("42")
    }

    @Test(expected = IllegalArgumentException::class)
    fun typeNotRegistered() {
        data class A(
            val x: Int
        ) : Action
        actionJson.encode(A(42))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noMessageTypeAnnotation() {
        data class A(
            val x: Int
        ) : Action
        actionJson.registerMessageType(A::class)
        println(actionJson.encode(A(42)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownType() {
        actionJson.decode("{\"type\":\"A\"}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingPayload() {
        actionJson.decode("{\"type\":\"ADD_TODO\"}")
    }
}
