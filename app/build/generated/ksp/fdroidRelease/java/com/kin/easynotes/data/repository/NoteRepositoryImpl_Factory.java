package com.kin.easynotes.data.repository;

import com.kin.easynotes.data.local.database.NoteDatabaseProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
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
public final class NoteRepositoryImpl_Factory implements Factory<NoteRepositoryImpl> {
  private final Provider<NoteDatabaseProvider> providerProvider;

  private NoteRepositoryImpl_Factory(Provider<NoteDatabaseProvider> providerProvider) {
    this.providerProvider = providerProvider;
  }

  @Override
  public NoteRepositoryImpl get() {
    return newInstance(providerProvider.get());
  }

  public static NoteRepositoryImpl_Factory create(Provider<NoteDatabaseProvider> providerProvider) {
    return new NoteRepositoryImpl_Factory(providerProvider);
  }

  public static NoteRepositoryImpl newInstance(NoteDatabaseProvider provider) {
    return new NoteRepositoryImpl(provider);
  }
}
