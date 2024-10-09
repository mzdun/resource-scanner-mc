// Copyright (c) 2024 Marcin Zdun
// This code is licensed under MIT license (see LICENSE for details)

package com.midnightbits.scanner.sonar.graphics;

import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

public class MatrixStack {
    private final Deque<MatrixStack.Entry> stack = new ArrayDeque<>();

    public MatrixStack(Matrix4f startingMatrix) {
        stack.add(new MatrixStack.Entry(new Matrix4f(startingMatrix)));
    }

    public void translate(float x, float y, float z) {
        MatrixStack.Entry entry = this.stack.getLast();
        entry.positionMatrix.translate(x, y, z);
    }

    public void push() {
        this.stack.addLast(new MatrixStack.Entry(this.stack.getLast()));
    }

    public void pop() {
        this.stack.removeLast();
    }

    public MatrixStack.Entry peek() {
        return this.stack.getLast();
    }

    public static final class Entry {
        final Matrix4f positionMatrix;

        Entry(Matrix4f positionMatrix) {
            this.positionMatrix = positionMatrix;
        }

        Entry(MatrixStack.Entry matrix) {
            this.positionMatrix = new Matrix4f(matrix.positionMatrix);
        }

        public Matrix4f getPositionMatrix() {
            return this.positionMatrix;
        }
    }
}
