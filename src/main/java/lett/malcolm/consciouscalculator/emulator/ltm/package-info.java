/**
 * This package holds logic related to Long Term Memory.
 * 
 * Within this emulator, Long Term Memory represents a number of things:
 * <ul>
 * <li> Pre-programmed and learned concepts and facts  -- this is the primary purpose of LTM
 * <li> Long memory of history                         -- secondary purpose of LTM
 * </ul>
 * 
 * Pre-programmed concepts and facts are represented in a mixture of generic LTM representation,
 * and hard-coded for practical simplicity.
 *
 * <h3>Code-level Access to Pre-programmed Concepts</h3>
 * Processors don't necessarily need to directly load and use Fact instances,
 * because many processors represent learned rapid evaluation of WM state.
 * Thus they do not need to access LTM.
 * 
 * But when high-order processing is required, that processing may need to go
 * back to base principles, and to access the linked original concept.
 * 
 * Thus rapid evaluation processors may access internal classes directly,
 * without going via instances of the fact.
 */
package lett.malcolm.consciouscalculator.emulator.ltm;
