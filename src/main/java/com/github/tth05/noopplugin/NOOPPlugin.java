package com.github.tth05.noopplugin;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import me.coley.recaf.plugin.api.ContextMenuInjectorPlugin;
import me.coley.recaf.ui.ContextBuilder;
import me.coley.recaf.util.ClassUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.plugface.core.annotations.Plugin;

@Plugin(name = "NO-OP")
public class NOOPPlugin implements ContextMenuInjectorPlugin {

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Allows you to no-op a method with a single click.";
    }

    @Override
    public void forMethod(ContextBuilder builder, ContextMenu menu, String owner, String name, String desc) {
        MenuItem clearItem = new MenuItem("Clear");

        clearItem.setOnAction((e) -> {
            Type returnType = Type.getType(desc).getReturnType();

            ClassWriter cw = builder.getController().getWorkspace().createWriter(ClassWriter.COMPUTE_FRAMES);
            ClassNode existingNode = ClassUtil.getNode(builder.getReader(), ClassReader.EXPAND_FRAMES);

            for (int i = 0; i < existingNode.methods.size(); i++) {
                MethodNode node = existingNode.methods.get(i);
                if (node.name.equals(name) && node.desc.equals(desc)) {
                    node.maxStack = 1;
                    node.maxLocals = 1;
                    node.tryCatchBlocks.clear();
                    node.localVariables.clear();
                    node.exceptions.clear();
                    node.instructions.clear();
                    node.instructions.add(getReturnOpCodes(returnType));

                    existingNode.methods.set(i, node);
                    break;
                }
            }

            existingNode.accept(cw);

            builder.getResource().getClasses().put(builder.getReader().getClassName(), cw.toByteArray());
            builder.getClassView().updateView();
        });

        menu.getItems().add(2, clearItem);
    }

    private InsnList getReturnOpCodes(Type type) {
        InsnList insnList = new InsnList();
        if (type == Type.VOID_TYPE) {
            insnList.add(new InsnNode(Opcodes.RETURN));
        } else if (type == Type.DOUBLE_TYPE) {
            insnList.add(new InsnNode(Opcodes.DCONST_0));
            insnList.add(new InsnNode(Opcodes.DRETURN));
        } else if (type == Type.FLOAT_TYPE) {
            insnList.add(new InsnNode(Opcodes.FCONST_0));
            insnList.add(new InsnNode(Opcodes.FRETURN));
        } else if (type == Type.LONG_TYPE) {
            insnList.add(new InsnNode(Opcodes.LCONST_0));
            insnList.add(new InsnNode(Opcodes.LRETURN));
        } else if (type == Type.INT_TYPE || type == Type.CHAR_TYPE || type == Type.BOOLEAN_TYPE ||
                type == Type.BYTE_TYPE || type == Type.SHORT_TYPE) {
            insnList.add(new InsnNode(Opcodes.ICONST_0));
            insnList.add(new InsnNode(Opcodes.IRETURN));
        } else {
            insnList.add(new InsnNode(Opcodes.ACONST_NULL));
            insnList.add(new InsnNode(Opcodes.ARETURN));
        }

        return insnList;
    }
}
