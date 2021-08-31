package me.maya.revolt.api

interface IUpdateable<T> {
    fun update(data: T): T
}