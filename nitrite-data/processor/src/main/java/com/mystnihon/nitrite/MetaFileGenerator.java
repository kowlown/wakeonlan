package com.mystnihon.nitrite;

import com.mystnihon.nitrite.metadata.Attribute;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaFileGenerator {
    public static final String CLASSNAME_SUFFIX = "_";
    public static final int MAX_SECURITY_DEPTH = 250;
    public static final String ATTRIBUTE_NAME_SEPARATOR = "_";
    public static final String ATTRIBUTE_DB_FIELD_SEPARATOR = ".";
    private final Types types;
    private final Elements elementUtils;
    private final Messager messager;

    public MetaFileGenerator(Types types, Elements elementUtils, Messager messager) {
        this.types = types;
        this.elementUtils = elementUtils;
        this.messager = messager;
    }

    public void generateCode(Element element, Filer filer) throws IOException {
        String packageName = "";//Empty default package name
        if (element.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
            PackageElement aPackage = (PackageElement) element.getEnclosingElement();
            packageName = aPackage.getQualifiedName().toString();
        }

        String className = element.getSimpleName().toString() + CLASSNAME_SUFFIX;
        messager.printMessage(Diagnostic.Kind.OTHER, "Classname: " + className);
        TypeSpec.Builder builder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);


        List<FieldSpec> fieldSpecs = getFieldSpecs(element, Collections.emptyList(), 0);
        fieldSpecs.forEach(builder::addField);

        TypeSpec typeSpecRegistration = builder.build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpecRegistration).build();

        javaFile.writeTo(filer);
    }

    private List<FieldSpec> getFieldSpecs(Element element, List<String> parentFieldNames, int depth) {
        log("Depth:" + depth + " for element:" + element.getSimpleName().toString());
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        if (depth > MAX_SECURITY_DEPTH) {
            warn("Max depth has been reached");
            return fieldSpecs;
        }
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD && enclosedElement.getModifiers().stream().noneMatch(modifier -> modifier.equals(Modifier.STATIC))) {
                //The name of the field.
                String fieldName = enclosedElement.getSimpleName().toString();

                //The type of the field
                Element enclosedElementType = types.asElement(enclosedElement.asType());

                //We get the typename of the field, automatically boxed if it's a primitive.
                TypeName boxedFieldTypeName = TypeName.get(enclosedElement.asType()).box(); //The type of the field. We box the field in case of the type is a primitive.

                //We get the typename of the field, with erasure of generics, automatically boxed if it's a primitive. Used to write the Type.class instruction.
                TypeName boxFieldTypeErasure = TypeName.get(types.erasure(enclosedElement.asType())).box();
                log("Typename:" + boxedFieldTypeName.toString());

                //We copy the parent fields name, so we don't corrupt the parent fields for the next fields
                List<String> strings = new ArrayList<>(parentFieldNames);

                //And add our field name
                strings.add(fieldName);

                //We build the values.
                String attributeFieldName = StringUtils.join(strings, ATTRIBUTE_NAME_SEPARATOR);
                String attributeDbFieldName = StringUtils.join(strings, ATTRIBUTE_DB_FIELD_SEPARATOR);

                FieldSpec fieldSpec = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Attribute.class), boxedFieldTypeName), attributeFieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer("new $T($S, $T.class)", Attribute.class, attributeDbFieldName, boxFieldTypeErasure).build();
                fieldSpecs.add(fieldSpec);

                //We will proceed to traversal for our children field
                if (!isPrimitive(boxedFieldTypeName) && !isForbidden(boxedFieldTypeName, enclosedElementType) && isNotCircularDependency(element, enclosedElementType)) {
                    List<FieldSpec> fieldSpecsSecond = getFieldSpecs(enclosedElementType, strings, depth + 1);
                    fieldSpecs.addAll(fieldSpecsSecond);
                }
            }
        }
        return fieldSpecs;
    }

    /**
     * log pre-compiler message
     *
     * @param message The message
     */
    private void log(String message) {
        messager.printMessage(Diagnostic.Kind.OTHER, message);
    }

    /**
     * warn pre-compiler message
     *
     * @param message the message
     */
    private void warn(@SuppressWarnings("SameParameterValue") String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message);
    }


    /**
     * We don't read attribute of class related to primitive
     *
     * @param typeName The typename of the primitive
     * @return whether we're are allow to traverse the class.
     */
    private boolean isPrimitive(TypeName typeName) {
        return typeName.isPrimitive() || typeName.isBoxedPrimitive();
    }

    /**
     * We fordid the read of attribute of some classes
     *
     * @param typeName The typename of the element
     * @param element  The element we would like to traverse
     * @return whether we're are allow to traverse the class.
     */
    private boolean isForbidden(TypeName typeName, Element element) {
        boolean assignable = false;
        if (element != null) {
            assignable = types.isAssignable(element.asType(), elementUtils.getTypeElement(Temporal.class.getName()).asType());
        }
        return TypeName.get(LocalTime.class).equals(typeName) || TypeName.get(String.class).equals(typeName) || assignable;
    }

    /**
     * To avoid circular dependency.
     * <p>Example:
     * <pre>
     * class Foo {
     *    private Foo bar;
     *    private String text;
     * }
     * </pre>
     * is a valid class, but the attribute <b>bar</b> would cause a circular dependency
     * </p>
     *
     * @param currentElement The current parent element
     * @param targetElement  The element we would like to traverse
     * @return whether we're are allow to traverse the class.
     */
    private boolean isNotCircularDependency(Element currentElement, Element targetElement) {
        return !currentElement.equals(targetElement);
    }

}
