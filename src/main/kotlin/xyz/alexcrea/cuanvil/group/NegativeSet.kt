package xyz.alexcrea.cuanvil.group

class NegativeSet<T>(val negate: MutableSet<T>) : MutableSet<T> {

    override fun iterator(): MutableIterator<T> {
        TODO("Not yet implemented") // can't be implemented I guess
    }

    override fun add(element: T): Boolean {
        return negate.remove(element)
    }

    override fun remove(element: T): Boolean {
        return negate.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return negate.removeAll(elements)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return negate.addAll(elements)
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override val size get() = TODO("Not yet implemented")

    override fun contains(element: T): Boolean {
        return !negate.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        for (elm in elements) {
            if(negate.contains(elm)) return false
        }

        return true
    }

}