package com.durganmcbroom.artifact.resolver

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public abstract class Lockable {
    public var isLocked: Boolean by locking(false)

    public fun lock() {
        if (!isLocked) isLocked = true
    }

    public fun <T> locking(initial: T): ReadWriteProperty<Lockable, T> = object : ReadWriteProperty<Lockable, T> {
        private var value: T = initial

        override fun getValue(thisRef: Lockable, property: KProperty<*>): T = value

        override fun setValue(thisRef: Lockable, property: KProperty<*>, value: T) {
            if (thisRef.isLocked) throw IllegalStateException("Cannot modify property '${property.name}' when locked!")

            this.value = value
        }

    }

    public inline fun <T : Any> lateInitLocking(crossinline exceptionProvider: () -> Exception): ReadWriteProperty<Lockable, T> = object : ReadWriteProperty<Lockable, T> {
        private var value: T? = null

        override fun getValue(thisRef: Lockable, property: KProperty<*>): T {
            if (value == null) throw exceptionProvider()
            return value!!
        }

        override fun setValue(thisRef: Lockable, property: KProperty<*>, value: T) {
            if (thisRef.isLocked) throw IllegalStateException("Cannot modify property '${property.name}' when locked!")

            this.value = value
        }
    }

    public inline fun <T : Any> nullableOrLateInitLocking(initial: T?, crossinline exceptionProvider: () -> Exception = { IllegalStateException("Value has not been initialized!") }): ReadWriteProperty<Lockable, T> =
        if (initial == null) lateInitLocking(exceptionProvider) else locking(initial)

    public inline fun <T : Any> lockingOr(crossinline provider: () -> T): ReadWriteProperty<Lockable, T> =
        object : ReadWriteProperty<Lockable, T> {
            private var value: T? = null

            override fun getValue(thisRef: Lockable, property: KProperty<*>): T {
                if (value == null) value = provider()
                return value as T
            }

            override fun setValue(thisRef: Lockable, property: KProperty<*>, value: T) {
                if (thisRef.isLocked) throw IllegalStateException("Cannot modify property '${property.name}' when locked!")

                this.value = value
            }
        }
}