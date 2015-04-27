package scotch.lang;

import static scotch.runtime.RuntimeSupport.applicable;
import static scotch.runtime.RuntimeSupport.callable;
import static scotch.symbol.type.Types.fn;
import static scotch.symbol.type.Types.var;

import scotch.data.string.String_;
import scotch.runtime.Applicable;
import scotch.runtime.RaisedException;
import scotch.symbol.Module;
import scotch.symbol.ReExportMember;
import scotch.symbol.ReExportModule;
import scotch.symbol.Value;
import scotch.symbol.ValueType;
import scotch.symbol.type.Type;

@SuppressWarnings("unused")
@Module(reExports = {
    @ReExportModule(moduleName = "scotch.data.int", members = {
        @ReExportMember(memberName = "Int"),
    }),
    @ReExportModule(moduleName = "scotch.data.bool", members = {
        @ReExportMember(memberName = "Bool"),
        @ReExportMember(memberName = "&&"),
    }),
    @ReExportModule(moduleName = "scotch.data.char", members = {
        @ReExportMember(memberName = "Char"),
    }),
    @ReExportModule(moduleName = "scotch.data.string", members = {
        @ReExportMember(memberName = "String"),
        @ReExportMember(memberName = "++"),
    }),
    @ReExportModule(moduleName = "scotch.data.double", members = {
        @ReExportMember(memberName = "Double"),
    }),
    @ReExportModule(moduleName = "scotch.data.num", members = {
        @ReExportMember(memberName = "Num"),
        @ReExportMember(memberName = "+"),
        @ReExportMember(memberName = "-"),
        @ReExportMember(memberName = "*"),
        @ReExportMember(memberName = "negate"),
        @ReExportMember(memberName = "-prefix"),
        @ReExportMember(memberName = "abs"),
        @ReExportMember(memberName = "signum"),
        @ReExportMember(memberName = "fromInteger"),
    }),
    @ReExportModule(moduleName = "scotch.data.eq", members = {
        @ReExportMember(memberName = "Eq"),
        @ReExportMember(memberName = "=="),
        @ReExportMember(memberName = "/="),
    }),
    @ReExportModule(moduleName = "scotch.data.function", members = {
        @ReExportMember(memberName = "$"),
        @ReExportMember(memberName = "."),
    }),
    @ReExportModule(moduleName = "scotch.data.maybe", members = {
        @ReExportMember(memberName = "Maybe"),
        @ReExportMember(memberName = "Just"),
        @ReExportMember(memberName = "Nothing"),
    }),
    @ReExportModule(moduleName = "scotch.data.list", members = {
        @ReExportMember(memberName = ":"),
        @ReExportMember(memberName = "[]"),
    }),
    @ReExportModule(moduleName = "scotch.control.monad", members = {
        @ReExportMember(memberName = "Monad"),
        @ReExportMember(memberName = ">>="),
        @ReExportMember(memberName = ">>"),
        @ReExportMember(memberName = "return"),
        @ReExportMember(memberName = "fail"),
    }),
    @ReExportModule(moduleName = "scotch.data.tuple", members = {
        @ReExportMember(memberName = "(,)"),
        @ReExportMember(memberName = "(,,)"),
        @ReExportMember(memberName = "(,,,)"),
        @ReExportMember(memberName = "(,,,,)"),
        @ReExportMember(memberName = "(,,,,,)"),
        @ReExportMember(memberName = "(,,,,,,)"),
        @ReExportMember(memberName = "(,,,,,,,)"),
        @ReExportMember(memberName = "(,,,,,,,,)"),
        @ReExportMember(memberName = "(,,,,,,,,,)"),
        @ReExportMember(memberName = "(,,,,,,,,,,)"),
        @ReExportMember(memberName = "(,,,,,,,,,,,)"),
        @ReExportMember(memberName = "(,,,,,,,,,,,,)"),
    }),
    @ReExportModule(moduleName = "scotch.data.ord", members = {
        @ReExportMember(memberName = "Ord"),
        @ReExportMember(memberName = "Ordering"),
        @ReExportMember(memberName = "LessThan"),
        @ReExportMember(memberName = "GreaterThan"),
        @ReExportMember(memberName = "EqualTo"),
        @ReExportMember(memberName = "min"),
        @ReExportMember(memberName = "max"),
        @ReExportMember(memberName = "compare"),
        @ReExportMember(memberName = ">"),
        @ReExportMember(memberName = ">="),
        @ReExportMember(memberName = "<"),
        @ReExportMember(memberName = "<="),
    }),
    @ReExportModule(moduleName = "scotch.data.either", members = {
        @ReExportMember(memberName = "Either"),
        @ReExportMember(memberName = "Left"),
        @ReExportMember(memberName = "Right"),
    })
})
public class ScotchModule {

    @Value(memberName = "raise")
    public static <A> Applicable<String, A> raise() {
        return applicable(message -> callable(() -> {
            throw new RaisedException(message.call());
        }));
    }

    @ValueType(forMember = "raise")
    public static Type raise$type() {
        return fn(String_.TYPE, var("a"));
    }
}
