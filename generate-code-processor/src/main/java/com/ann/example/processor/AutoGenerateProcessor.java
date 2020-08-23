package com.ann.example.processor;

import com.ann.example.annotation.AutoImplement;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes(
        {"com.ann.example.annotation.AutoImplement"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoGenerateProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "AutoGenerateProcessor @gabe ");
        if (annotations.size() == 0) {
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoImplement.class);


        List<String> uniqueIdCheckList = new ArrayList<>();

        for (Element element : elements) {
            AutoImplement autoImplement = element.getAnnotation(AutoImplement.class);

            if (element.getKind() != ElementKind.INTERFACE) {
                error("The annotation @AutoImplement can only be applied on interfaces: ",
                        element);

            } else {
                boolean error = false;

                if (uniqueIdCheckList.contains(autoImplement.as())) {
                    error("AutoImplement#as should be uniquely defined", element);
                    error = true;
                }

                error = !checkIdValidity(autoImplement.as(), element);

                if (!error) {
                    uniqueIdCheckList.add(autoImplement.as());
                    try {
                        generateClass(autoImplement, element);
                    } catch (Exception e) {
                        error(e.getMessage(), null);
                    }
//
//                    try {
//                        generateClassPoet(autoImplement, element);
//                    } catch (Exception e) {
//                        error(e.getMessage(), null);
//                    }
                }
            }
        }
        return false;
    }
////        Some control flow statements, such as if/else, can have unlimited control flow possibilities. You can handle those options using nextControlFlow():
//
//        MethodSpec main = MethodSpec.methodBuilder("main")
//                .addStatement("long now = $T.currentTimeMillis()", System.class)
//                .beginControlFlow("if ($T.currentTimeMillis() < now)", System.class)
//                .addStatement("$T.out.println($S)", System.class, "Time travelling, woo hoo!")
//                .nextControlFlow("else if ($T.currentTimeMillis() == now)", System.class)
//                .addStatement("$T.out.println($S)", System.class, "Time stood still!")
//                .nextControlFlow("else")
//                .addStatement("$T.out.println($S)", System.class, "Ok, time still moving forward")
//                .endControlFlow()
//                .build();
//        Which generates:
//
//        void main() {
//            long now = System.currentTimeMillis();
//            if (System.currentTimeMillis() < now)  {
//                System.out.println("Time travelling, woo hoo!");
//            } else if (System.currentTimeMillis() == now) {
//                System.out.println("Time stood still!");
//            } else {
//                System.out.println("Ok, time still moving forward");
//            }
//        }

    //                MethodSpec total = MethodSpec.methodBuilder("total")
//                .addCode(""
//                        + "int total = 0;\n"
//                        + "for (int i = 0; i < 10; i++) {\n"
//                        + "  total += i;\n"
//                        + "}\n")
//                .build();


    //    MethodSpec main = MethodSpec.methodBuilder("main")
//            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//            .returns(void.class)
//            .addParameter(String[].class, "args")
//            .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
//            .build();
    //123行

    /**
     * 用java poet生成代码。
     * @param autoImplement
     * @param element
     * @throws Exception
     */
    private void generateClassPoet(AutoImplement autoImplement, Element element)
            throws Exception {
        String pkg = getPackageName(element);
        //delegate some processing to our FieldInfo class
        FieldInfo fieldInfo = FieldInfo.get(element);
        //the target interface name
        String interfaceName = getTypeName(element);
        ClassName userGabeClsName = ClassName.get(pkg, autoImplement.as() + "Gabe");
        ClassName userBuilderClsName = ClassName.get(pkg + "." + autoImplement.as() + "Gabe", autoImplement.as() + "GabeBuilder");
        //自定义的class interface不能反射。
        ClassName interfaceClsName = ClassName.get(pkg, interfaceName);
        //两个final 成员变量
        List<String> tMandatoryFields = fieldInfo.getMandatoryFields();

        TypeSpec.Builder userClsBuilder = TypeSpec.classBuilder(userGabeClsName.simpleName())
                .addSuperinterface(interfaceClsName)
                .addModifiers(Modifier.PUBLIC);
        //UserGabe成员变量 及其get方法。
        for (Map.Entry<String, String> entry : fieldInfo.getFields().entrySet()) {
            String name = entry.getKey();
            String type = entry.getValue();
            boolean mandatory = tMandatoryFields.contains(name);
            Class tCls = Class.forName(type);
            if (mandatory) {
                FieldSpec tField = FieldSpec.builder(tCls, name).addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();
                userClsBuilder.addField(tField);
            } else {
                userClsBuilder.addField(tCls, name, Modifier.PRIVATE);
            }
            MethodSpec tFieldMethod = MethodSpec.methodBuilder("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(tCls)
                    .addStatement("return " + name)
                    .build();
            userClsBuilder.addMethod(tFieldMethod);
        }
        //UserGabe构造函数
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        for (String arg : tMandatoryFields) {
            constructorBuilder = constructorBuilder.addParameter(String.class, arg);
            constructorBuilder = constructorBuilder.addStatement("this.$N = $N", arg, arg);
        }
        MethodSpec constructorMethod = constructorBuilder.build();
        userClsBuilder.addMethod(constructorMethod);
        //内部类 UserGabeBuilder
        TypeSpec.Builder innerClsBuilder = null;
        if (autoImplement.builder()) {
            innerClsBuilder = TypeSpec.classBuilder(userBuilderClsName.simpleName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
            //内部类UserGabeBuilder成员变量 及其set方法。
            for (Map.Entry<String, String> entry : fieldInfo.getFields().entrySet()) {
                String name = entry.getKey();
                String type = entry.getValue();
                Class tCls = Class.forName(type);
                FieldSpec tField = FieldSpec.builder(tCls, name).addModifiers(Modifier.PRIVATE).build();
                innerClsBuilder.addField(tField);
                boolean mandatory = tMandatoryFields.contains(name);
                if(!mandatory){
                    MethodSpec tFieldMethod = MethodSpec.methodBuilder(name)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(tCls,name)
                            .returns(userBuilderClsName)
                            .addStatement("this.$N = $N", name, name)
                            .addStatement("return this")
                            .build();
                    innerClsBuilder.addMethod(tFieldMethod);
                }
            }
            //UserGabeBuilder 的create 方法
            MethodSpec.Builder createBuilder = MethodSpec.methodBuilder("create")
                    .addModifiers(Modifier.PUBLIC,Modifier.STATIC);
            String newStatementForCreate =  userBuilderClsName.reflectionName()+" a = new "+userBuilderClsName.reflectionName()+"(";
            int cnt = tMandatoryFields.size();
            for(int i = 0;i<cnt;i++){
                if(i==(cnt-1)){
                    newStatementForCreate = newStatementForCreate + tMandatoryFields.get(i);
                }else{
                    newStatementForCreate = newStatementForCreate +tMandatoryFields.get(i)+",";
                }
                 createBuilder.addParameter(String.class, tMandatoryFields.get(i));
            }
            newStatementForCreate +=")";
            createBuilder.addStatement( newStatementForCreate);
            createBuilder.returns(userBuilderClsName).addStatement("return a");
            MethodSpec createMethod = createBuilder.build();
            innerClsBuilder.addMethod(createMethod);
            //复用了 UserGabe的构造函数
            innerClsBuilder.addMethod(constructorMethod);
            //UserGabeBuilder build 方法
            MethodSpec.Builder builderMethod = MethodSpec.methodBuilder("build");
            String newStatementForBuilder =  userGabeClsName.reflectionName()+" a = new "+userGabeClsName.reflectionName()+"(";
            int argCnt = tMandatoryFields.size();
            for(int i = 0;i<argCnt;i++){
                if(i==(argCnt-1)){
                    newStatementForBuilder = newStatementForBuilder + tMandatoryFields.get(i);
                }else{
                    newStatementForBuilder = newStatementForBuilder +tMandatoryFields.get(i)+",";
                }
            }
            newStatementForBuilder +=")";
            builderMethod.addStatement(newStatementForBuilder);
            for (Map.Entry<String, String> entry : fieldInfo.getFields().entrySet()) {
                String name = entry.getKey();
                boolean mandatory = tMandatoryFields.contains(name);
                if(!mandatory){
                    builderMethod.addStatement("a."+name +"="+name);
                }
            }
            builderMethod.returns(userGabeClsName);
            builderMethod.addStatement( "return a");
            innerClsBuilder.addMethod(builderMethod.build());
        }
        userClsBuilder.addType(innerClsBuilder.build()); // 设置内部类
        TypeSpec annClass = userClsBuilder.build();
        JavaFile javaFile = JavaFile.builder(pkg, annClass).build();
        try {
            javaFile.writeTo(System.out);
            generateClass(userGabeClsName.reflectionName(), javaFile.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//98行

    /**
     * 用StringBuffer直接写代码。
     * @param autoImplement
     * @param element
     * @throws Exception
     */
    private void generateClass(AutoImplement autoImplement, Element element)
            throws Exception {
        String pkg = getPackageName(element);
        //delegate some processing to our FieldInfo class
        FieldInfo fieldInfo = FieldInfo.get(element);
        //the target interface name
        String interfaceName = getTypeName(element);
        //using our JClass to delegate most of the string appending there
        JClass implClass = new JClass();
        implClass.definePackage(pkg);
        implClass.defineClass("public class ", autoImplement.as(), "implements " + interfaceName);
        //nested builder class
        JClass builder = null;
        String builderClassName = null;
        if (autoImplement.builder()) {
            builder = new JClass();
            builder.defineClass("public static class",
                    builderClassName = autoImplement.as() + "Builder", null);
        }
        //adding class fields
        implClass.addFields(fieldInfo.getFields());
        if (builder != null) {
            builder.addFields(fieldInfo.getFields());
        }
        //adding constructor with mandatory fields
        implClass.addConstructor(builder == null ? "public" : "private",
                fieldInfo.getMandatoryFields());
        if (builder != null) {
            builder.addConstructor("private", fieldInfo.getMandatoryFields());
        }
        //generate methods
        for (Map.Entry<String, String> entry : fieldInfo.getFields().entrySet()) {
            String name = entry.getKey();
            String type = entry.getValue();
            boolean mandatory = fieldInfo.getMandatoryFields().contains(name);
            implClass.createGetterForField(name);
            //if no builder generation specified then crete setters for non mandatory fields
            if (builder == null && !mandatory) {
                implClass.createSetterForField(name);
            }
            if (builder != null && !mandatory) {
                builder.addMethod(new JMethod()
                        .defineSignature("public", false, builderClassName)
                        .name(name)
                        .addParam(type, name)
                        .defineBody(" this." + name + " = " + name + ";"
                                + JClass.LINE_BREAK
                                + " return this;"
                        )
                );
            }
        }
        if (builder != null) {
            //generate create() method of the Builder class
            JMethod createMethod = new JMethod()
                    .defineSignature("public", true, builderClassName)
                    .name("create");
            String paramString = "(";
            int i = 0;
            for (String s : fieldInfo.getMandatoryFields()) {
                createMethod.addParam(fieldInfo.getFields().get(s), s);
                paramString += (i != 0 ? ", " : "") + s;
                i++;
            }
            paramString += ");";
            createMethod.defineBody("return new " + builderClassName
                    + paramString);

            builder.addMethod(createMethod);
            //generate build() method of the builder class.
            JMethod buildMethod = new JMethod()
                    .defineSignature("public", false, autoImplement.as())
                    .name("build");
            StringBuilder buildBody = new StringBuilder();
            buildBody.append(autoImplement.as())
                    .append(" a = new ")
                    .append(autoImplement.as())
                    .append(paramString)
                    .append(JClass.LINE_BREAK);
            for (String s : fieldInfo.getFields().keySet()) {
                if (fieldInfo.getMandatoryFields().contains(s)) {
                    continue;
                }
                buildBody.append("a.")
                        .append(s)
                        .append(" = ")
                        .append(s)
                        .append(";")
                        .append(JClass.LINE_BREAK);
            }
            buildBody.append("return a;")
                    .append(JClass.LINE_BREAK);
            buildMethod.defineBody(buildBody.toString());
            builder.addMethod(buildMethod);
            implClass.addNestedClass(builder);
        }
        //finally generate class via Filer
        generateClass(pkg + "." + autoImplement.as(), implClass.end());
    }

    private String getPackageName(Element element) {
        List<PackageElement> packageElements =
                ElementFilter.packagesIn(Arrays.asList(element.getEnclosingElement()));
        Optional<PackageElement> packageElement = packageElements.stream().findAny();
        return packageElement.isPresent() ?
                packageElement.get().getQualifiedName().toString() : null;
    }

    private void generateClass(String qfn, String end) throws IOException {
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qfn);
        Writer writer = sourceFile.openWriter();
        writer.write(end);
        writer.close();
    }

    /**
     * Checking if the class to be generated is a valid java identifier
     * Also the name should be not same as the target interface
     */
    private boolean checkIdValidity(String name, Element e) {
        boolean valid = true;
        for (int i = 0; i < name.length(); i++) {
            if (i == 0 ? !Character.isJavaIdentifierStart(name.charAt(i)) :
                    !Character.isJavaIdentifierPart(name.charAt(i))) {
                error("AutoImplement#as should be valid java " +
                        "identifier for code generation: " + name, e);
                valid = false;
            }
        }
        if (name.equals(getTypeName(e))) {
            error("AutoImplement#as should be different than the Interface name ", e);
        }
        return valid;
    }

    /**
     * Get the simple name of the TypeMirror
     */
    private static String getTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String[] split = typeMirror.toString().split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }

    private void error(String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}