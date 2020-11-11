package com.bread.auth.config;

import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationRequestPartFactory;
import org.springframework.restdocs.operation.preprocess.ContentModifier;
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.UriModifyingOperationPreprocessor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.HOST;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

@Setter
public class CustomUriModifyingOperationPreprocessor extends UriModifyingOperationPreprocessor {

    private final UriModifyingContentModifier contentModifier = new UriModifyingContentModifier();

    private final OperationPreprocessor contentModifyingDelegate = new ContentModifyingOperationPreprocessor(contentModifier);

    private String scheme;

    private String host;

    private String port;

    private String basePath;

    private HttpHeaders modify(HttpHeaders headers) {
        HttpHeaders modified = new HttpHeaders();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            for (String value : header.getValue()) {
                modified.add(header.getKey(), contentModifier.modify(value));
            }
        }
        return modified;
    }

    private Collection<OperationRequestPart> modify(Collection<OperationRequestPart> parts) {
        List<OperationRequestPart> modifiedParts = new ArrayList<>();
        OperationRequestPartFactory factory = new OperationRequestPartFactory();
        for (OperationRequestPart part : parts) {
            modifiedParts.add(
                    factory.create(
                            part.getName(),
                            part.getSubmittedFileName(),
                            contentModifier.modifyContent(
                                    part.getContent(),
                                    part.getHeaders()
                                            .getContentType()
                            ),
                            modify(part.getHeaders())
                    )
            );
        }
        return modifiedParts;
    }

    @Override
    public OperationRequest preprocess(OperationRequest request) {
        URI uri = request.getUri();
        UriComponentsBuilder uriBuilder = fromUri(uri);
        if (hasText(scheme)) {
            uriBuilder.scheme(scheme);
        }
        if (hasText(host)) {
            uriBuilder.host(host);
        }
        if (hasText(basePath)) {
            uriBuilder.replacePath(basePath + uri.getPath());
        }
        if (hasText(port)) {
            uriBuilder.port(port);
        }
        URI modifiedUri = uriBuilder
                .build(true)
                .toUri();
        HttpHeaders modifiedHeaders = modify(request.getHeaders());
        String uriPort = modifiedUri.getPort() != -1 ? ":" + modifiedUri.getPort() : "";
        modifiedHeaders.set(
                HOST,
                modifiedUri.getHost() + uriPort
        );
        request = new OperationRequestFactory()
                .create(
                        uriBuilder
                                .build(true)
                                .toUri(),
                        request.getMethod(),
                        request.getContent(),
                        modifiedHeaders,
                        request.getParameters(),
                        modify(request.getParts()),
                        request.getCookies()
                );
        return contentModifyingDelegate.preprocess(request);
    }

    @Setter
    private static final class UriModifyingContentModifier implements ContentModifier {

        private static final Pattern SCHEME_HOST_PORT_PATTERN = Pattern.compile("(http[s]?)://([^/:#?]+)(:[0-9]+)?");

        private String scheme;

        private String host;

        private String port;

        @Override
        public byte[] modifyContent(byte[] content, MediaType contentType) {
            String input;
            if (contentType != null && contentType.getCharset() != null) {
                input = new String(content, contentType.getCharset());
            } else {
                input = new String(content);
            }

            return modify(input).getBytes();
        }

        private String modify(String input) {
            List<String> replacements = asList(
                    this.scheme,
                    this.host,
                    hasText(this.port) ? ":" + this.port : this.port
            );
            int previous = 0;
            Matcher matcher = SCHEME_HOST_PORT_PATTERN.matcher(input);
            StringBuilder builder = new StringBuilder();
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.start(i) >= 0) {
                        builder.append(input, previous, matcher.start(i));
                    }
                    if (matcher.start(i) >= 0) {
                        previous = matcher.end(i);
                    }
                    builder.append(getReplacement(matcher.group(i), replacements.get(i - 1)));
                }
            }

            if (previous < input.length()) {
                builder.append(input.substring(previous));
            }
            return builder.toString();
        }

        private String getReplacement(String original, String candidate) {
            if (candidate != null) {
                return candidate;
            }
            if (original != null) {
                return original;
            }
            return "";
        }

    }

}
