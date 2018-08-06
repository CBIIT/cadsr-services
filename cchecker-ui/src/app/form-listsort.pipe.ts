import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'formListsort'
})
export class FormListsortPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    return value.sort((a, b) => typeof(a[args])=='string' ? (a[args].toUpperCase()<b[args].toUpperCase() ? -1 : 1) : (a[args]<b[args] ? -1:1));
  }

}
