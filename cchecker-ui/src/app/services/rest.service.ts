import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from './../../environments/environment';

@Injectable({
  providedIn: 'root'
})

export class RestService {
  REST_API:string;

  constructor(private http:HttpClient, private router:Router) {
    this.REST_API = environment.REST_API;
  }

  // generate excel report //
  generateExcel = () => this.http.get(`${this.REST_API}/gateway/genexcelreporterror`,
  {
    observe:'response',
    responseType: "blob",
    withCredentials:true
  }
  );

  // validation service //
  checkForms(checkedItems,formListData){
    const checkUom = formListData['checkUom'] ? 'true':'false';
    const checkCRF = formListData['checkStdCrfCde'] ? 'true':'false';
    const displayExceptions = formListData['mustDisplayException'] ? 'true':'false';
    return this.http.post(`${this.REST_API}/gateway/checkservice?checkCRF=${checkCRF}`,checkedItems,
    {
      withCredentials:true
    })
  } ; 

  // upload file service //
  uploadAlsFile(file, name){
    return this.http.post(`${this.REST_API}/gateway/parseservice?owner=${name}`,file,
    {
      observe:"events",
      reportProgress:true,
      withCredentials:true,
    })} ;

  // gets validation status //
  validateFeedStatus() {
    return this.http.get(`${this.REST_API}/gateway/feedvalidatestatus`,
    {
      observe:'events',
      reportProgress:true,
      responseType: "text",
      withCredentials:true
    });
  }




}
