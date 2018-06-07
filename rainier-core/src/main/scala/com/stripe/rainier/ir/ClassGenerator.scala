package com.stripe.rainier.ir

import java.io.File
import org.apache.commons.io.FileUtils

import com.stripe.rainier.internal.asm.Opcodes._
import com.stripe.rainier.internal.asm.tree.{ClassNode, MethodNode}
import com.stripe.rainier.internal.asm.ClassWriter

private[ir] trait ClassGenerator {

  val bytes: Array[Byte] = writeBytecode(createClass)

  def name: String
  def superClasses: Array[String]
  def methods: Seq[MethodNode]

  def writeToTmpFile(): Unit =
    FileUtils.writeByteArrayToFile(new File("/tmp/" + name + ".class"), bytes)

  private def createClass: ClassNode = {
    val cls = new ClassNode()
    cls.visit(V1_5,
              ACC_PUBLIC | ACC_SUPER,
              name,
              null,
              "java/lang/Object",
              superClasses)
    cls.methods.add(createInit)
    methods.foreach { m =>
      cls.methods.add(m)
    }
    cls
  }

  private def createInit: MethodNode = {
    val m = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null)
    m.visitCode()
    m.visitVarInsn(ALOAD, 0)
    m.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
    m.visitInsn(RETURN)
    m.visitMaxs(1, 1)
    m.visitEnd()
    m
  }

  protected def createConstantMethod(name: String, value: Int): MethodNode = {
    val m = new MethodNode(ACC_PUBLIC, name, "()I", null, null)
    m.visitLdcInsn(value)
    m.visitInsn(IRETURN)
    m
  }

  private def writeBytecode(classNode: ClassNode): Array[Byte] = {
    val cw = new ClassWriter(
      ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS)
    classNode.accept(cw)
    cw.toByteArray
  }
}

private[ir] object ClassGenerator {
  @volatile private var id: Int = 0
  def freshName: String = this.synchronized {
    val name = "CompiledFunction$" + id
    id += 1
    name
  }
}
