package me.xtrm.unlok.accessor

interface MethodAccessor<T> {
    fun invoke(vararg args: Any): T
}