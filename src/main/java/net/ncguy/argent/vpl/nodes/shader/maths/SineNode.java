package net.ncguy.argent.vpl.nodes.shader.maths;

import net.ncguy.argent.vpl.VPLGraph;
import net.ncguy.argent.vpl.annotations.NodeData;
import net.ncguy.argent.vpl.compiler.IShaderNode;
import net.ncguy.argent.vpl.nodes.shader.VPLShaderNode;

import java.lang.reflect.Method;

import static net.ncguy.argent.vpl.VPLPin.Types.*;

/**
 * Created by Guy on 10/09/2016.
 */
@NodeData(value = "Sine",tags = "shader")
public class SineNode extends VPLShaderNode<Float> {

    public SineNode(VPLGraph graph) {
        super(graph);
    }

    public SineNode(VPLGraph graph, Method method) {
        super(graph, method);
    }

    @Override
    protected void buildInput() {
        addPin(inputTable, float.class, "", INPUT);
    }

    @Override
    protected void buildOutput() {
        addPin(outputTable, float.class, "Result", OUTPUT, COMPOUND);
    }

    @Override
    protected void discernType() {
        discernType(float.class, 1);
    }

    @Override
    public void resetCache() {
        fragUsed = false;
    }
    boolean fragUsed;

    @Override
    public String getUniforms() {
        return "";
    }

    @Override
    public String getVariable(int pinId) {
        IShaderNode node = getNodePacker(0);
        if(node == null)
            return "";
        return String.format("sin(%s)", node.getVariable(this));
    }

    @Override
    public String getFragment() {
        if(fragUsed) return "";
        fragUsed = true;
        IShaderNode node = getNodePacker(0);
        if(node != null)
            return node.getFragment();
        return "";
    }
}
