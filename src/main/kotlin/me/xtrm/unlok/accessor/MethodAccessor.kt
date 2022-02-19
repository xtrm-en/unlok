package me.xtrm.unlok.accessor

interface MethodAccessor<O, T> {
    fun invoke(owner: O, vararg args: Any): T
}