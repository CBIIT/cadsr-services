import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { DataService } from './data.service';

@Injectable({
  providedIn: 'root'
})

export class RestService {
  constructor(private http:HttpClient) {
  }

  // upload file service //
  uploadAlsFile = file => this.http.post('http://localhost:8080/gateway/parseservice?owner=me2',file,
  {
    observe:"events",
    reportProgress:true
  });
}
