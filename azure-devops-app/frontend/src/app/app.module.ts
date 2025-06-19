import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms'; // Added FormsModule

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
// LoginComponent and RegisterComponent are standalone, no need to declare here

@NgModule({
  declarations: [
    AppComponent
    // LoginComponent and RegisterComponent are standalone
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule, // Added FormsModule for general use
    ReactiveFormsModule // Added ReactiveFormsModule for general use
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
