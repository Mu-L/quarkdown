package com.quarkdown.core.ast.quarkdown.invisible

import com.quarkdown.core.ast.Node
import com.quarkdown.core.document.slides.Transition
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A non-visible node that injects properties that affect the global configuration for slides documents.
 * If not specified, the default values of the underlying renderer are used.
 * @param centerVertically whether slides should be centered vertically
 * @param showControls whether navigation controls should be shown
 * @param showNotes whether speaker notes should be shown when not in speaker view
 * @param transition global transition between slides
 */
class SlidesConfigurationInitializer(
    val centerVertically: Boolean?,
    val showControls: Boolean?,
    val showNotes: Boolean?,
    val transition: Transition?,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
