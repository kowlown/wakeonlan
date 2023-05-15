package com.mystnihon.nitrite;

import com.google.auto.service.AutoService;
import com.mystnihon.nitrite.annotations.Collection;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;


@AutoService(Processor.class)
public class MetadataFieldProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private MetaFileGenerator metaFileGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        metaFileGenerator = new MetaFileGenerator(typeUtils, elementUtils, messager);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement typeElement : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                createMetaFile(element);
            }
        }
        return true;
    }

    private void createMetaFile(Element element) {
        try {
            metaFileGenerator.generateCode(element, filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Generation error: IOException");
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Collection.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
