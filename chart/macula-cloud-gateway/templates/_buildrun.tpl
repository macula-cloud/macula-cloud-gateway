{{- define "buildrunenv" -}}
{{- range $key0, $val0 := .Values.application }}
    {{- if kindIs  "map" $val0  }}
      {{- range $key1, $val1 := $val0 }}
        {{- if kindIs  "map" $val1  }}
          {{- range $key2, $val2 := $val1 }}
            {{- if kindIs  "map" $val2  }}
              {{- range $key3, $val3 := $val2 }}
                {{- if kindIs  "map" $val3  }}
                  {{- range $key4, $val4 := $val3 }}
                    {{- if kindIs  "map" $val4  }}
                      {{- range $key5, $val5 := $val4 }}
                        {{- if kindIs  "map" $val5  }}
                          {{- range $key6, $val6 := $val5 }}
                            {{- if kindIs  "map" $val6  }}
                              {{- range $key7, $val7 := $val6 }}
                                {{- if kindIs  "map" $val7  }}
                                  {{- range $key8, $val8 := $val7 }}
                                    {{- if kindIs  "map" $val8  }}
                                      {{- range $key9, $val9 := $val8 }}
                                        {{- if kindIs  "map" $val9  }}
                                          {{- range $key10, $val10 := $val9 }}
                                          {{- end }}
                                        {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}_{{ $key3 | upper }}_{{ $key4 | upper }}_{{ $key5 | upper }}_{{ $key6 | upper }}_{{ $key7 | upper }}_{{ $key8 | upper }}_{{ $key9 | upper }}
  value: {{ $val9 | quote }}
                                        {{- end }}
                                      {{- end }}
                                    {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}_{{ $key3 | upper }}_{{ $key4 | upper }}_{{ $key5 | upper }}_{{ $key6 | upper }}_{{ $key7 | upper }}_{{ $key8 | upper }}
  value: {{ $val8 | quote }}
                                    {{- end }}
                                  {{- end }}
                                {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}_{{ $key3 | upper }}_{{ $key4 | upper }}_{{ $key5 | upper }}_{{ $key6 | upper }}_{{ $key7 | upper }}
  value: {{ $val7 | quote }}
                                {{- end }}
                              {{- end }}
                            {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}_{{ $key3 | upper }}_{{ $key4 | upper }}_{{ $key5 | upper }}_{{ $key6 | upper }}
  value: {{ $val6 | quote }}
                            {{- end }}
                          {{- end }}
                        {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}_{{ $key3 | upper }}_{{ $key4 | upper }}_{{ $key5 | upper }}
  value: {{ $val5 | quote }}
                        {{- end }}
                      {{- end }}
                    {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}_{{ $key3 | upper }}_{{ $key4 | upper }}
  value: {{ $val4 | quote }}
                    {{- end }}
                  {{- end }}
                {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}_{{ $key3 | upper }}
  value: {{ $val3 | quote }}
                {{- end }}
              {{- end }}
            {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}_{{ $key2 | upper }}
  value: {{ $val2 | quote }}
            {{- end }}
          {{- end }}
        {{- else }}
- name: {{ $key0 | upper }}_{{ $key1 | upper }}
  value: {{ $val1 | quote }}
        {{- end }}
      {{- end }}
    {{- else }}
- name: {{ $key0 | upper }}
  value: {{ $val0 | quote }}
    {{- end }}
{{- end }}
{{- end }}
