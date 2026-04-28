package com.kin.easynotes.presentation.components;

import android.content.Context;
import android.os.Handler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class GalleryObserver_Factory implements Factory<GalleryObserver> {
  private final Provider<Handler> handlerProvider;

  private final Provider<Context> contextProvider;

  private GalleryObserver_Factory(Provider<Handler> handlerProvider,
      Provider<Context> contextProvider) {
    this.handlerProvider = handlerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public GalleryObserver get() {
    return newInstance(handlerProvider.get(), contextProvider.get());
  }

  public static GalleryObserver_Factory create(Provider<Handler> handlerProvider,
      Provider<Context> contextProvider) {
    return new GalleryObserver_Factory(handlerProvider, contextProvider);
  }

  public static GalleryObserver newInstance(Handler handler, Context context) {
    return new GalleryObserver(handler, context);
  }
}
