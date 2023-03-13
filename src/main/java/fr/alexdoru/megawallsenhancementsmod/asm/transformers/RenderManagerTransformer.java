package fr.alexdoru.megawallsenhancementsmod.asm.transformers;

import fr.alexdoru.megawallsenhancementsmod.asm.IMyClassTransformer;
import fr.alexdoru.megawallsenhancementsmod.asm.InjectionStatus;
import fr.alexdoru.megawallsenhancementsmod.asm.mappings.ClassMapping;
import fr.alexdoru.megawallsenhancementsmod.asm.mappings.FieldMapping;
import fr.alexdoru.megawallsenhancementsmod.asm.mappings.MethodMapping;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class RenderManagerTransformer implements IMyClassTransformer {

    @Override
    public String getTargetClassName() {
        return "net.minecraft.client.renderer.entity.RenderManager";
    }

    @Override
    public ClassNode transform(ClassNode classNode, InjectionStatus status) {

        status.setInjectionPoints(10);

        for (final MethodNode methodNode : classNode.methods) {

            if (checkMethodNode(methodNode, MethodMapping.RENDERMANAGER$INIT)) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (checkInsnNode(insnNode, ICONST_0) && checkFieldInsnNode(insnNode.getNext(), PUTFIELD, FieldMapping.RENDERMANAGER$DEBUGBOUNDINGBOX)) {
                        /*
                         * Replaces in the constructor :
                         * this.debugBoundingBox = false;
                         * With :
                         * this.debugBoundingBox = ConfigHandler.isDebugHitboxOn;
                         */
                        methodNode.instructions.insert(insnNode, getNewConfigFieldInsnNode("isDebugHitboxOn"));
                        methodNode.instructions.remove(insnNode);
                        status.addInjection();
                    }
                }
            }

            if (checkMethodNode(methodNode, MethodMapping.RENDERDEBUGBOUNDINGBOX)) {
                /*
                 * Injects at head :
                 * if(RenderManagerHook.cancelHitboxRender(entityIn)) {
                 *     return;
                 * }
                 */
                methodNode.instructions.insert(getCancelRenderInsnList());
                status.addInjection();

                int count255 = -1;

                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {

                    if (checkVarInsnNode(insnNode, ALOAD, 12)) {
                        count255 = 0;
                        /*
                         * Replaces line 451 :
                         * RenderGlobal.drawOutlinedBoundingBox(axisalignedbb1, 255, 255, 255, 255);
                         * With :
                         * RenderGlobal.drawOutlinedBoundingBox(
                         *              RenderManagerHook.getAxisAlignedBB(axisalignedbb1, entityIn),
                         *              RenderManagerHook.getRedHitboxColor(255, this.textRenderer, entityIn),
                         *              RenderManagerHook.getGreenHitboxColor(255),
                         *              RenderManagerHook.getBlueHitboxColor(255),
                         *              255
                         * );
                         */
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 1));
                        list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("RenderManagerHook"), "getAxisAlignedBB", "(L" + ClassMapping.AXISALIGNEDBB + ";L" + ClassMapping.ENTITY + ";)L" + ClassMapping.AXISALIGNEDBB + ";", false));
                        methodNode.instructions.insert(insnNode, list);
                        status.addInjection();
                    }

                    if (count255 > -1 && count255 < 3 && checkIntInsnNode(insnNode, SIPUSH, 255)) {
                        if (count255 == 0) {
                            final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("RenderManagerHook"), "getRedHitboxColor", "(IL" + ClassMapping.ENTITY + ";)I", false));
                            methodNode.instructions.insert(insnNode, list);
                            status.addInjection();
                        } else if (count255 == 1) {
                            methodNode.instructions.insert(insnNode, new MethodInsnNode(INVOKESTATIC, getHookClass("RenderManagerHook"), "getGreenHitboxColor", "(I)I", false));
                            status.addInjection();
                        } else {
                            methodNode.instructions.insert(insnNode, new MethodInsnNode(INVOKESTATIC, getHookClass("RenderManagerHook"), "getBlueHitboxColor", "(I)I", false));
                            status.addInjection();
                        }
                        count255++;
                    }

                    if (checkTypeInsnNode(insnNode, INSTANCEOF, ClassMapping.ENTITYLIVINGBASE)) {
                        final AbstractInsnNode nextNode = insnNode.getNext();
                        if (checkJumpInsnNode(nextNode, IFEQ)) {
                            final LabelNode labelNode = ((JumpInsnNode) nextNode).label;
                            /*
                             * Transforms line 453 :
                             * if (entityIn instanceof EntityLivingBase)
                             * Becomes :
                             * if (entityIn instanceof EntityLivingBase && ConfigHandler.drawRedBox)
                             */
                            final InsnList list = new InsnList();
                            list.add(new JumpInsnNode(IFEQ, labelNode));
                            list.add(getNewConfigFieldInsnNode("drawRedBox"));
                            methodNode.instructions.insert(insnNode, list);
                            status.addInjection();
                        }
                    }

                    if (insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst.equals(new Double("2.0"))) {
                        /*
                         * Line 464
                         * Replaces the 2.0D with RenderManagerHook.getBlueVectLength(entityIn);
                         */
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 1)); // load entity
                        list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("RenderManagerHook"), "getBlueVectLength", "(L" + ClassMapping.ENTITY + ";)D", false));
                        methodNode.instructions.insertBefore(insnNode, list);
                        methodNode.instructions.remove(insnNode);
                        status.addInjection();
                    }
                }

            }
        }
        return classNode;
    }

    private InsnList getCancelRenderInsnList() {
        final InsnList list = new InsnList();
        final LabelNode notCancelled = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 1)); // load entity
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(getNewFieldInsnNode(GETFIELD, FieldMapping.RENDERMANAGER$LIVINGENTITY));
        list.add(new MethodInsnNode(INVOKESTATIC, getHookClass("RenderManagerHook"), "cancelHitboxRender", "(L" + ClassMapping.ENTITY + ";L" + ClassMapping.ENTITY + ";)Z", false)); // load the boolean
        list.add(new JumpInsnNode(IFEQ, notCancelled)); // if (true) { return;} else {jump to notCancelled label}
        list.add(new InsnNode(RETURN)); // return;
        list.add(notCancelled);
        return list;
    }

}