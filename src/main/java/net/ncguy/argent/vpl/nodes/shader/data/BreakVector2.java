package net.ncguy.argent.vpl.nodes.shader.data;

import com.badlogic.gdx.math.Vector2;
import net.ncguy.argent.vpl.VPLGraph;
import net.ncguy.argent.vpl.annotations.NodeData;
import net.ncguy.argent.vpl.compiler.IShaderNode;
import net.ncguy.argent.vpl.nodes.shader.VPLShaderNode;

import java.lang.reflect.Method;

import static net.ncguy.argent.vpl.VPLPin.Types.*;

/**
 * Created by Guy on 07/09/2016.
 */
@NodeData(value = "Break Vector2", execIn = false, execOut = false, tags = "shader")
public class BreakVector2 extends VPLShaderNode<Float> {

    public BreakVector2(VPLGraph graph) {
        super(graph);
    }

    public BreakVector2(VPLGraph graph, Method method) {
        super(graph, method);
    }

    @Override
    protected void buildInput() {
        addPin(inputTable, Vector2.class, "Vector2", INPUT);
    }

    @Override
    protected void buildOutput() {
        addPin(outputTable, float.class, "X", OUTPUT, COMPOUND);
        addPin(outputTable, float.class, "Y", OUTPUT, COMPOUND);
    }

    @Override
    protected void discernType() {
        discernType(float.class, 2);
    }

    @Override
    public String getUniforms() {
        return "";
    }

    @Override
    public String getVariable(int pinId) {
        IShaderNode node = getNodePacker(0);
        if(node == null) return "";
        switch(pinId) {
            case 0: return node.getVariable(this)+".x";
            case 1: return node.getVariable(this)+".y";
        }
        return "";
    }

    @Override
    public String getFragment() {
        IShaderNode node = getNodePacker(0);
        if(node == null) return "";
        return node.getFragment();
    }
}
