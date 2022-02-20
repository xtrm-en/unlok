package me.xtrm.unlok.api.accessor

interface MethodAccessor<T> {
    fun invoke(vararg args: Any): T
}