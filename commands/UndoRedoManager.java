package commands;

import java.io.*;
import java.util.Stack;

/**
 * Controla o historico de undo/redo usando duas pilhas de snapshots.
 * Cada snapshot e uma copia profunda e independente do estado (feita via
 * serializacao Java para bytes), entao alterar o objeto original depois
 * de salvar o estado nao afeta o que ja foi empilhado -- isso e essencial
 * pra o undo restaurar exatamente o estado de antes do comando, mesmo que
 * o objeto tenha sido mutado in-place em seguida.
 *
 * Uso tipico na Facade: antes de qualquer comando que altera o sistema,
 * chama saveState(estadoAtual); se o usuario pedir undo(), essa foto e
 * devolvida e o estado atual vai pra pilha de redo (e vice-versa). Dar um
 * novo saveState() depois de um undo limpa a pilha de redo, porque o
 * "futuro" que foi desfeito deixa de existir.
 */
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

    // Serializa o objeto para um array de bytes independente -- e isso
    // que garante a copia profunda (o objeto original pode continuar
    // sendo alterado sem afetar o que foi salvo aqui).
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