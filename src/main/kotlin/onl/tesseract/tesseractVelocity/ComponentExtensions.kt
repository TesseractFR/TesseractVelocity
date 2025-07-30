package onl.tesseract.tesseractVelocity

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.Translatable

operator fun Component.plus(text: String): Component {
    return this.append(Component.text(text))
}

operator fun Component.plus(translatable: Translatable): Component {
    return this.append(Component.translatable(translatable))
}

operator fun Translatable.plus(text: String): Component {
    return Component.translatable(this) + text
}

operator fun Component.plus(component: Component): Component {
    return this.append(component)
}

operator fun Component.plus(text: Char): Component {
    return this.append(Component.text(text))
}

operator fun TextColor.plus(text: String): Component {
    return Component.text(text, this)
}

fun Component.append(text: String): Component {
    return this.append(Component.text(text))
}

fun Component.append(text: String, color: TextColor): Component {
    return this.append(Component.text(text, color))
}
fun Component.append(int: Int, color: TextColor): Component {
    return this.append(Component.text(int, color))
}

fun Component.append(text: String, color: TextColor, decoration: TextDecoration): Component {
    return this.append(Component.text(text, color, decoration))
}

fun Component.append(
    text: String,
    color: TextColor,
    decoration1: TextDecoration,
    decoration2: TextDecoration,
): Component {
    return this.append(Component.text(text, color, decoration1, decoration2))
}

fun Component.appendNewLine(): Component {
    return this.append(Component.newline())
}

fun String.toComponent(): TextComponent = Component.text(this)