import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'separatebycharacter'
})
export class SeparatebycharacterPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    let v = value;
    console.log(args)
    if (value) {
      v = value.replace(/\|/g, "<br />");
    };
    return v;
  }

}
