package scotch.lang;

import scotch.symbol.Module;
import scotch.symbol.ReExportMember;
import scotch.symbol.ReExportModule;

@SuppressWarnings("unused")
@Module(reExports = {
    @ReExportModule(moduleName = "scotch.data.int", members = {
        @ReExportMember(memberName = "Int"),
    }),
    @ReExportModule(moduleName = "scotch.data.bool", members = {
        @ReExportMember(memberName = "Bool"),
    }),
    @ReExportModule(moduleName = "scotch.data.char", members = {
        @ReExportMember(memberName = "Char"),
    }),
    @ReExportModule(moduleName = "scotch.data.string", members = {
        @ReExportMember(memberName = "String"),
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

}
