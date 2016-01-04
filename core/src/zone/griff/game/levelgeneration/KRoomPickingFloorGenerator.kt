package zone.griff.game.levelgenerationss

import zone.griff.game.levelgeneration.IntVector2
import kotlin.MutableSet
import kotlin.collections.*
import kotlin.ranges.downTo
import kotlin.text.split

class Array2D<T> (val width: Int, val height: Int, val array: Array<Array<T?>>) {

    companion object {
        inline operator fun <reified T> invoke(width: Int, height: Int) =
                Array2D(width, height, Array(width) {
                    Array<T?>(height) { null }
                })
    }

    operator fun get(x: Int, y: Int): T? {
        return array[x][y]
    }

    operator fun set(x: Int, y: Int, t: T) {
        array[x][y] = t
    }

    inline fun forEach(operation: (T?) -> Unit) {
        array.forEach { it.forEach { operation(it) } }
    }

    inline fun forEachIndexed(operation: (x: Int, y: Int, T?) -> Unit) {
        array.forEachIndexed { x, p ->
            p.forEachIndexed { y, t -> operation(x, y, t) }
        }
    }
}

data class MapRoom(val x: Int, val y: Int, val width: Int, val height: Int) {
    inline fun forEach(operation: (x: Int, y: Int) -> Unit) {
        (x..x+width-1).forEach { ix -> (y..y+height-1).forEach { iy -> operation(ix, iy) } }
    }
    inline fun forEachAdjacent(operation: (x: Int, y: Int) -> Unit) {
        val left   = x
        val right  = x + width - 1
        val top    = y + height - 1
        val bottom = y
        (left     ..   right ).forEach { operation(it,        bottom - 1) }
        (bottom   ..   top   ).forEach { operation(right + 1, it        ) }
        (right  downTo left  ).forEach { operation(it,        top + 1   ) }
        (top    downTo bottom).forEach { operation(left - 1,  it        ) }
    }
}

fun mapRoomFromString(doorString: String, x: Int, y: Int): MapRoom {
    val split = doorString.split("c")
    val width = split[0].length
    val height = split[1].length
    return MapRoom(x, y, width, height)
}

data class MapDoor(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    init {
        val dx = Math.abs(x1 - x2)
        val dy = Math.abs(y1 - y2)
        assert((dx === 1 && dy === 0) || (dx === 0 && dy === 1)) {
            "Door positions must be adjacent: ($x1, $y1), ($x2, $y2)"
        }
    }
}

class MapRoomOrDoor() {
    var room: MapRoom? = null
    private val doors: MutableSet<MapDoor> = hashSetOf()
    fun hasDoor(): Boolean {
        return doors.count() > 0
    }
    fun doors(): Set<MapDoor> {
        return doors;
    }
    fun addDoor(door: MapDoor) {
        doors.forEach { assert(it != door) {"Can't have 2 doors overlap completely"} }
        assert(doors.count() < 4) {"Can't have more than 4 doors at one location"}
        doors.add(door)
    }
}

fun Array2D<MapRoomOrDoor>.getOrCreate(x: Int, y: Int): MapRoomOrDoor {
    var retVal = this[x, y]
    if (retVal == null) {
        retVal = MapRoomOrDoor()
        this[x, y] = retVal
    }
    return retVal
}

fun Array2D<MapRoomOrDoor>.forEachDoor(room: MapRoom, operation: (MapDoor) -> Unit) {
    room.forEach { x, y ->
        this.getOrCreate(x, y).doors().forEach(operation)
    }
}

fun Array2D<MapRoomOrDoor>.add(room: MapRoom): MapRoom {
    room.forEach { x, y ->
        val v = this.getOrCreate(x, y)
        assert(v.room == null) {"Can't add a room on top of another room"}
        v.room = room
    }
    return room
}

fun Array2D<MapRoomOrDoor>.add(door: MapDoor): MapDoor {
    val a = this.getOrCreate(door.x1, door.y1)
    val b = this.getOrCreate(door.x2, door.y2)

    assert(a.room != null || b.room != null) {"At least one side of a door must have a room"}

    a.addDoor(door)
    b.addDoor(door)

    return door
}

fun Array2D<MapRoomOrDoor>.add(doorString: String, x: Int, y: Int): MapRoom {
    return this.add(mapRoomFromString(doorString, x, y))
    // TODO: doorStrings don't actually create doors yet.
}

fun Array2D<MapRoomOrDoor>.placeToAdd(room: MapRoom, door: MapDoor): IntVector2 {
    val retVal = IntVector2()

    val g1 = this.getOrCreate(door.x1, door.y1)
    val g2 = this.getOrCreate(door.x2, door.y2)

    assert((g1.room == null) xor (g2.room == null)) { "Exactly one side of the door must have a room" }

    val doorTarget = if (g1.room == null) g1 else g2
    val doorOrigin = if (g1.room == null) g2 else g1

    // TODO

    return retVal
}

class KRoomPickingFloorGenerator {
    val map = Array2D<MapRoomOrDoor>(101, 100)
    val initialRoom = map.add(MapRoom(x = 49, y = 49, width = 3, height = 2))

    val availableDoorStrings = setOf(
            "dcocococ",
            "dcdcococ",
            "ococdcoc",
            "ocococdc",

            "ocdcocdc",

            "dcdcocdc",
            "ocdcdcdc"
    )
}