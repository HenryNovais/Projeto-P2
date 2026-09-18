package commands;

import java.io.*;
import java.util.Stack;

public class UndoRedoManager {
    private Stack<byte[]> undoStack = new Stack<>();
    private Stack<byte[]> redoStack = new Stack<>();

    public void saveState(Object state) {
        undoStack.push(serialize(state));
        redoStack.clear();
    }

    public Object undo(Object currentState) throws Exception {
        if (undoStack.isEmpty()) throw new Exception("Nao ha comando a desfazer.");
        redoStack.push(serialize(currentState));
        return deserialize(undoStack.pop());
    }

    public Object redo(Object currentState) throws Exception {
        if (redoStack.isEmpty()) throw new Exception("Nao ha comando a refazer.");
        undoStack.push(serialize(currentState));
        return deserialize(redoStack.pop());
    }

    private byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar: " + e.getMessage());
        }
    }

    private Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar: " + e.getMessage());
        }
    }
}